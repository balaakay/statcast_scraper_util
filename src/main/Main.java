package main;

import java.net.http.*;
import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.PrintWriter;

public class Main {

  // deletes files in the data directory after running the statcast_processing
  // defaults to false to keep the files
  public static boolean can_delete_files = false;
  public static boolean new_table_needed = true;
  private static String database_url = "";
  private static String database_user = "";
  private static String database_password = "";
  private static boolean set_search_path = true;


  /**
   * decides if the search_path of the given user should be changed to the
   * savant_data schema.
   *
   * @param set : sets search_path to savant_data if true, does not change
   *            search_path if false
   */
  public static void set_search_path(boolean set) {
    set_search_path = set;
  }

  
  /**
   * classic setter for database_url
   *
   * @param url : database url set to class variable database_url
   */
  public static void set_database_url(String url) {
    database_url = url;
  }


  /**
   * classic setter for database_user
   *
   * @param user : username for PosgreSQL DB
   * @return void
   */
  public static void set_database_user(String user) {
    database_user = user;
  }


  public static void set_database_password(String password) {
    database_password = password;
  }


  /**
   *  classic getter for the database_user
   *
   *  @return String of database_user name
   */
  public static String get_database_user() {
    return database_user;
  }


  /**
   * sets class variable new_table_needed to boolean parameter. This is used to
   * delete old tables in Postgres in case a new dataset is trying to be imported
   *
   * @param is_needed : boolean to decide if deleting old tables is correct.
   *                  set to true by default
   */
  public static void set_new_table_needed(boolean is_needed) {
    new_table_needed = is_needed;
  }


  // TODO: keep default to false??
  /**
   * sets can_delete_files class variable to the parameter is_deletable. Allows
   * for the deletion of all files created in the api request. Leaving files is
   * mainly for the increased speed of import into the postgres DB if imports
   * are regular.
   *
   * @param is_deleteable : set to false by default. May change later.
   */
  public static void delete_temp_files(boolean is_deleteable) {
    can_delete_files = is_deleteable;
  }


  /**
   * handles getting data from baseball savant API and saving all data to csv
   * files in the data directory.
   * This will eventually handle different date ranges but currently only handles
   * a season at a time.
   *
   * @param season : the MLB season you wish to pull data for
   */
  public static void multiple_file_statcast_processing(int season) throws URISyntaxException, IOException {
    String[][] schedule_date_groups = schedule_processor.generateSeasonDates(season);

    for (String[] dates : schedule_date_groups) {
      try (PrintWriter pw = statcast_scraper.printWriter_creator()) {
        URI baseball = statcast_scraper.createURI(dates[0], dates[1]);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(baseball)
            .build();
        HttpResponse<String> response = statcast_scraper.pingStatcastSearch(request);
        System.out.println("Response for " + dates[0] + " recieved.");
        statcast_scraper.save_to_csv(response, pw);
      } catch (Exception e) {
        e.getMessage();
        e.printStackTrace();
      }
    }
  }

  /**
   * main function handler for easy usage as a utility package. passes season year
   * so the multiple_file_statcast_processing function can take it. creates DB
   * connections to your postgres instance. and creates schema/table if they do
   * not already exist. After importing data from API, postgres COPY function
   * is used to copy all data from the CSV files to the DB.
   *
   * @param season_year : season from which you want the data.
   */
  public static void run_statcast_import_processing(int season_year) throws SQLException, 
         URISyntaxException, IOException {
    database_handler db = new database_handler();
    db.connect_to_database(database_url, database_user, database_password);
    statcast_scraper.set_game_types_queried("PO", "R");

    if (!db.schema_exists()) {
      db.create_schema();
    }

    boolean does_table_exist = db.table_exists();
    System.out.println(does_table_exist);
    if (new_table_needed && does_table_exist) {
      db.delete_base_statcast();
      System.out.println("table deleted");
    }

    does_table_exist = db.table_exists();
    if (!does_table_exist) {
      System.out.println("table does not yet exist");
      db.create_table();
    } else {
      System.out.println("Schema and base_statcast table both exist");
    }

    // TODO: solve the issue of running this multiple times for multiple seasons
    multiple_file_statcast_processing(season_year);

    db.batch_copy_from_csv(System.getProperty("user.dir") + "/data/", can_delete_files);

    // TODO: This is currently not working, will have to figure this out later
    //db.reorder_base_statcast();


    if(set_search_path) {
      db.set_search_path();
    }

  }



  public static void main(String[] args) throws SQLException, URISyntaxException, IOException {
    set_database_url("jdbc:postgresql://localhost:5432/statcast");
    set_database_user("balaakay");
    set_database_password("password");
    run_statcast_import_processing(2024); 
  }
}
