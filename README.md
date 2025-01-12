# statcast_scraper
Scraper for all historical statcast data written in Java.

NOTE: This is my first attempt at a publicly available project. The code is
probably bad. It works for me so please contribute or add issues if something
doesn't work.

NOTE: This has only been developed and tested on Linux (ubuntu). Please let me
know if you get this working on MacOS or Windows(ew).

# usage

set_database_url(String) - used to connect to a specific postgreSQL DB instance.

    set_database_url("jdbc:postgresql://localhost:3000/database");


set_database_user(String) - user connecting to database.

    set_database_user("user");


set_database_password(String) - password associated with the user set in
set_database_user(String)
    
    set_database_password("password");


AFTER all those functions have been called you can now call the main function
of the package:

run_statcast_import_processing(int) - main function for the utility. Pass in
the year you want to create a table of.
    
    run_statcast_import_processing(2024);





Helper functions:

delete_temp_files(boolean) - deletes all CSV's created by data import process
if set to true. Defaults to false.

    delete_temp_files(true);



set_new_table_needed(boolean) - if set to true, deletes the old base_statcast
table so a new one is created during the process on its next run.

    set_new_table_needed(true);



set_search_path(boolean) - Defaults to true. If true, will take the database
user's search_path in postgres and change it to savant_data so the user doesn't
have to enter "...from savant_data.base_statcast..." every query.

    set_search_path(true);



rename_base_statcast(String) - MUST BE CALLED AFTER
'run_statcast_import_processing()'. can rename the base_statcast table to
whatever argument is passed.

    rename_base_statcast("new_table");



# documentation


### supported game types
The same as the CSV documentation for savant search
R:regular season, PO:post season, E:exhibition, D:divisional series, W:world
series, S:spring training, F:wild card, L:league champ series

This package will change the default search_path for the user to savant_data.
This is meant to help but you can disable this feature with
"set_search_path(false)" as shown in the 'usage' section above.

# TODO


- Handle variable size date ranges, not just full seasons
- only rerun HTTP requests if the file for that given date range does not exist
  in the current data directory
- directories that you put this utility package in could have issues with not
  being executable directories. Create an issue if this happens to you.
- testing suite... I have not created any comprehensive tests for this
  software. Help me out if you want to.
- can I multithread these HTTP requests/writes to files? This is clearly the
  most time consuming aspect of this process.
- adding a row_id to the resulting table after ordering the games/ab's/pitches.
- handle SQL notices from sql connections and pass it to system.out.println()
