package main;

import java.net.http.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class statcast_scraper {


  public enum game_types {
    R, PO, S, E, F, D, L, W
  }


  //Class variables
  static ArrayList<game_types> game_list = new ArrayList<>();
  static int fileCounter = 0;
  static HttpClient client = HttpClient.newHttpClient();
  static final String dir_path = System.getProperty("user.dir") + "/data";
  static final String filePath = System.getProperty("user.dir") + "/data/data_";
  static final String fileExtension = ".csv";


  /**
   *  creates a string used in the API url designating which game types will be
   *  queried.
   *
   *  @return returns the URL styled string for game types.
   */
  public static String get_game_types_queried() {
    String insert_into_uri = "";
    for (int i = 0; i < game_list.size(); i++) {
      insert_into_uri = insert_into_uri + game_list.get(i) + "%%7C";
    }
    return insert_into_uri;
  }
  

  /**
   *  takes variable quantity of parameters and if they match the game_types enum
   *  then they are inserted into the class ArrayList game_types for later processing.
   *
   *  @param strings : variable length parameter; can be any string that matches 
   *  game_types enum. 
   *  @throws IllegalArgumentException : throws this exception when string does 
   *  not match any of the acceptable game_types enum values
   */
  public static void set_game_types_queried(String ...strings) throws 
      IllegalArgumentException {
    int size = strings.length;
    for (int i = 0; i < size; i++) {
      switch (strings[i]) {
        case "PO":
          game_list.add(game_types.PO);
          break;
        case "R":
          game_list.add(game_types.R);
          break;
        case "E":
          game_list.add(game_types.E);
          break;
        case "S":
          game_list.add(game_types.S);
          break;
        case "F":
          game_list.add(game_types.F);
          break;
        case "D":
          game_list.add(game_types.D);
          break;
        case "L":
          game_list.add(game_types.L);
          break;
        case "W":
          game_list.add(game_types.W);
          break;
        default:
          throw new IllegalArgumentException("The String input here is not a "
              + "valid game type: " + strings[i]);
      }
    }
  }


  /**
   *  attempts to send an {@link HttpRequest} passed into the parameters. 
   *
   *  @param request : an HttpRequest
   */
  public static HttpResponse<String> pingStatcastSearch(HttpRequest request) {
    HttpResponse<String> response = null;
    try {
      response = client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    return response;
  }

  /**
   *  Creates a {@link PrintWriter} from a file and a number. used to 
   *  dynamically make temp files for the copy from csv system in postgres
   *
   *  @param file : {@link File} base file.
   *  @param number : the number used to diferentiate the new subfiles in
   *      a season processing.
   *  @return new {@link PrintWriter}
   */
  public static PrintWriter printWriter_creator() throws
    FileNotFoundException {
      String fullPath = filePath + fileCounter + fileExtension;

      File directory = new File(dir_path);
      if(!directory.exists()) {
        directory.mkdirs();
      }

      PrintWriter pw = new PrintWriter(fullPath);
      // increment file counter for next invocation of the function
      fileCounter = fileCounter + 1;
      return pw;
    }


  /**
   *  appends the body of the {@link HttpResponse} to the end of the {@link PrintWriter}
   *
   *  @param response : Http response from pinging baseball_savant's statcast search API
   *  @param pw : open printwriter used to write data to CSV file.
   */
  public static void save_to_csv(HttpResponse<String> response, PrintWriter pw) {
    // Check for a null response that could be created in pingStatcastSearch()
    if(response == null) {
      return;
    }
    String body = response.body();
    pw.append(body);
    return;
  }


  /**
   *  formats the URI string into a workable URI that will be used to send HTTP 
   *  Requests. 
   *
   *  @param start_date : start date for the API request
   *  @param end_date : end date for the API request
   *  @throws URISyntaxException : if URI is invalid, throw this exception
   */
  public static URI createURI(String start_date, String end_date) throws URISyntaxException {
    String savant = "https://baseballsavant.mlb.com/statcast_search/csv?";
    String games = get_game_types_queried();
    String newSearch = "all=true&hfPT=&hfAB=&hfBBT=&hfPR=&hfZ=&stadium=" +
      "&hfBBL=&hfNewZones=&hfGT=" + games + "=&hfC=&hfSea=&hfSit=&" +
      "hfOuts=&opponent=&pitcher_throws=&batter_stands=&hfSA=&player_type=&" +
      "hfInfield=&team=&position=&hfOutfield=&hfRO=&home_road=&game_date_gt=%s&" + // start date
      "game_date_lt=%s&hfFlag=&hfPull=&metric_1=&hfInn=&min_pitches=0&" + // end date
      "min_results=0&group_by=name&sort_col=pitches&" +
      "player_event_sort=h_launch_speed&sort_order=desc&min_abs=0&type=details&";

    String fullParams = String.format(newSearch, start_date, end_date);
    String fullURI = savant + fullParams;
    return new URI(fullURI);
  }


  public static void main(String[] args) throws 
    URISyntaxException, IOException, FileNotFoundException,
    SecurityException {

      // testing code

      //URI baseball = createURI();
      //
      //if(!csv.exists()) {
      //    csv.createNewFile();
      //}
      //
      //PrintWriter pw = new PrintWriter(csv);
      //
      //HttpRequest request = HttpRequest.newBuilder()
      //    .uri(baseball)
      //    .build();
      //
      // HttpResponse<String> response = pingStatcastSearch(request);
      // save_to_csv(response, pw);
    }
}
