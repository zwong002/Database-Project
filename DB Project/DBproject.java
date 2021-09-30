/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
		try{	
			System.out.print("\t Enter make: "); 
			String make_in;
			do{
				try{
					make_in = in.readLine();
					if(make_in.length() <= 0 || make_in.length()>32){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid!");
					continue;
				}
			}while(true);
			
			System.out.print("\t Enter model: ");
			String model_in;
			do{
				try{
					model_in = in.readLine(); 
					if(model_in.length() <=0 || model_in.length() >64){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid!");
					continue;
				}
			}while(true);
			
			System.out.print("\t Enter age_year: ");
			int age_in;
			do{
				try{
					String age_year = in.readLine(); 
					age_in = Integer.parseInt(age_year);
					if(age_in <=0){
						throw new RuntimeException();
				}
				break;
				}catch (Exception e) {
					System.out.println("Your input is invalid! Try again");
					continue;
				}//end try
			}while(true);
			
			System.out.print("\t Enter seats: ");
			int seats_in;
			do{
				try{
					String seats = in.readLine(); 
					seats_in = Integer.parseInt(seats);
					if(seats_in <= 0) {
						throw new RuntimeException();
					}
					break;
				}catch (Exception e) {
					System.out.println("Your input is invalid!");
					continue;
				}//end try
			}while(true);
			
			String query = "INSERT INTO Plane (make, model, age, seats) VALUES(\'"+ make_in + "\', \'" + model_in + "\' ," + age_in + ", " + seats_in + ");";
			System.out.println(query);
			int rowCount = esql.executeQuery(query);
			System.out.println("total row(s): " + rowCount);
			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
		
	}
		
			
	public static void AddPilot(DBproject esql) {//2
		try{
			System.out.print("\t Enter Pilot fullname: "); 
			String pname;
			do{
				try{
					pname = in.readLine();
					if(pname.length() <=0 || pname.length() > 128){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid!");
					continue;
				}
			}while(true);
			
			System.out.print("\t Enter nationality: ");
			String pnationality; 
			do{
				try{
					pnationality = in.readLine();
					if(pnationality.length() <= 0 || pnationality.length() > 24){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid! Please enter valid input: ");
					continue;
				}
			}while(true);
			
			String query = "INSERT INTO Pilot (fullname, nationality) VALUES(\'"+ pname + "\', \'" + pnationality + "\');";
			System.out.println(query);
			int rowCount = esql.executeQuery(query);
			System.out.println("total row(s): " + rowCount);
			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		int num_s = 0;
		String comma = ", ";
			String apos = "'";
			String cost, num_sold, num_stops, departure, arrival, to, from, plane_id, pilot_id;
			//try{
			
			
			System.out.println("You've chosen to add new flight to DB!");
			
	//String query1 = "INSERT INTO Flight(cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) Values (";
			
			do{
				try{
					System.out.print("\t Enter cost: ");
					cost = in.readLine();
					if(Integer.parseInt(cost) <= 0 || cost.length() <= 0){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid! Please enter valid input");
					continue;
				}
			}while(true);
				
				
			do{
				try{
			
					System.out.print("\t Enter number sold: ");
			
					num_sold = in.readLine(); 
					//num_s = Integer.parseInt(num_sold);
					//query1 += num_sold;
					//query1 += comma;
					
					if(Integer.parseInt(num_sold) < 0 || num_sold.length() <= 0){
						throw new RuntimeException();
					}
						break;
				}catch(Exception a){
					System.out.println("Your input is invalid! Please enter valid input");
					continue;
				}
			}while(true);
			
			
			do{
				try{
					
					System.out.print("\t Enter number of stops: ");
					num_stops = in.readLine(); 
					//query1 += num_stops;
					//query1 += comma;
					if(Integer.parseInt(num_stops) < 0 || num_stops.length() <= 0){
						throw new RuntimeException();
					}
					break;
				}catch(Exception b){
					System.out.println("Your input is invalid! Please enter valid input");
					continue;
				}
			}while(true);
					
					
					//DEPARTURE
			
				
			do{
				
				try{
					String pattern = "yyyy-MM-dd";
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
					System.out.print("\t Enter departure date(yyyy-mm-dd): ");
					simpleDateFormat.setLenient(false);
					departure = in.readLine();
					Date date = simpleDateFormat.parse(departure);
			
					//query1 += apos;
					//query1 += departure;
					//query1 += apos;
					//query1 += comma;
					break;
				}catch(Exception c){
					System.out.println("Invalid Date Format, please enter in format (yyyy-mm-dd)");
					continue;
				}
			}while(true);
			 
			//ARRIVAL DATE
			do{
		
				try{
					String pattern = "yyyy-MM-dd";
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
					
					System.out.print("\t Enter arrival date(yyyy-mm-dd): ");
					
					/*System.out.print("\t Enter arrival year(yyyy): ");
					String year = in.readLine();
					System.out.print("\t Enter arrival month(mm): ");
					String month = in.readLine();
					System.out.print("\t Enter arrival day(dd): ");
					String year = in.readLine();
					*/
					simpleDateFormat.setLenient(false);
					arrival = in.readLine();
					Date date2 = simpleDateFormat.parse(arrival);
					//query1 += apos;
					//query1 += arrival;
					//query1 += apos;
					//query1 += comma;
					break;
					}catch(Exception c){
						System.out.println("Invalid Date Format: ");
						continue;
					}
				}while(true);	
				
				//ARRIVAL DESTINATION
				
			do{
				try{
					
			
					System.out.print("\t Enter arrival airport: ");
					to = in.readLine();
					//query1 += apos;
					//query1 += to;
					//query1 += apos;
					//query1 += comma;
					if(to.length() != 5){
						throw new RuntimeException();
					}
					break;
				}catch(Exception d){
					System.out.println("Invalid airport code, enter a 5-digit numeric code");
					continue;
				}
			}while(true);
				
			
			//String from = "";
			//while(from.length() != 5){
			
			do{
				try{
					
					System.out.print("\t Enter departure airport: ");
					from = in.readLine();
					//query1 += apos;
					//query1 += from;
					//query1 += apos;
					//query1 += ");";
					if(from.length() != 5){
						throw new RuntimeException();
					}
					break;
				}catch(Exception d){
					System.out.println("Invalid airport code, enter a 5-digit numeric code");
					continue;
				}
			}while(true);
		
			//System.out.println("test 1");
			//System.out.println(query1);
			//int rowCount1 = esql.executeQuery(query1); 
			//System.out.println("total row(s): " + rowCount1);
			//}catch(Exception d){
			//System.err.println(d.getMessage());
			//}	

			//String query = "INSERT INTO FlightInfo(pilot_id, plane_id) Values ( "; //updated to have trigger for both flight_id and fiid
			
			
			//System.out.print("\t Enter flight id: ");
			//query += apos;
			//String flight_id = in.readLine(); 
			//query += flight_id;
			//query += apos;
			//query += comma;
			
			do{
				try{

					System.out.print("\t Enter pilot id: ");
					//query += apos;
					pilot_id = in.readLine(); 
				
					String testQuery = "SELECT * FROM Pilot WHERE id = ";
					testQuery += pilot_id;
					testQuery += ";";
					if(esql.executeQuery(testQuery) < 1){	
						throw new RuntimeException();
					}
					break;
				}catch(Exception f){
					System.out.println("This Pilot does not exist, enter a valid pilot id");
					continue;
				}
			}while(true);
			
			//query += pilot_id;
			//query += apos;
			//query += comma;
			
			do{
				try{
					System.out.print("\t Enter plane_id: ");
			
					//query += apos;
					plane_id = in.readLine(); 
			
					String testQuery2 = "SELECT * FROM Plane WHERE id = ";
					testQuery2 += plane_id;
					testQuery2 += ";";
					
					if(esql.executeQuery(testQuery2) < 1){	
						throw new RuntimeException();
					}
					break;
				}catch(Exception g){
					System.out.println("This Pilot does not exist, enter a valid pilot id");
					continue;
				}
			}while(true);
			//int test2 = esql.executeQuery(testQuery);
			/*if(esql.executeQuery(testQuery2) < 1){
				System.out.println("This Plane does not exist, deleting flight created using non-existent plane.");
				String delete = "DELETE FROM Flight WHERE fnum IN (SELECT MAX(fnum) FROM Flight limit 1);";
				int del = esql.executeQuery(delete);
				return;
			}
			*/
			//query += plane_id;
			//query += apos;

			
			
			//query += ");";
			/*
			
			
			
				
				String delete = "DELETE FROM Flight WHERE fnum IN (SELECT MAX(fnum) FROM Flight limit 1);";
				int del = esql.executeQuery(delete);
				return;
			}*/
			
			do{
				try{
					num_s = Integer.parseInt(num_sold);
					System.out.println("Checking to see if seats sold doesn't exceed seats on plane");
					
					String query3 = "SELECT seats FROM Plane WHERE id = ";
					query3 += plane_id;
					query3 += ";";
					List<List<String>> L = esql.executeQueryAndReturnResult(query3);
					int t = Integer.parseInt(L.get(0).get(0));
					if(t < num_s){
						throw new RuntimeException();
					}
					break;
				}catch(Exception h){
					System.out.println("Number of seats sold exceeds seats available, enter new valid number of seats sold: ");
				}
				
				do{
					try{
					
						System.out.print("\t Enter number of seats sold: ");
						num_sold = in.readLine(); 
						//query1 += num_stops;
						//query1 += comma;
						if(Integer.parseInt(num_sold) < 0 || num_sold.length() <= 0){
							throw new RuntimeException();
						}	
						break;					
					}catch(Exception i){
						System.out.println("Your input is invalid! Please enter valid input");
						continue;
					}
				}while(true);
			
			}while(true);			
					
		do{
					
			try{
				String check = "Cost: " + cost + "\nSeats sold: " + num_sold + "\nDeparture date: " + departure + "\nArrival date: " + arrival + "\nArrival airport: "  + to + "\nDeparture airport: " + from + "\nPilot ID: " + pilot_id + "\nPlane ID: "  + plane_id;  
				
				System.out.println("Would you like to add this flight?");
				System.out.println(check);
				System.out.println("Enter 'yes' or 'no' ");
				String confirm = in.readLine();
				
				if(!confirm.equals("no") && !confirm.equals("yes") ){
					throw new RuntimeException();
				}else if(confirm.equals("no")){
					return;
				}else{
					System.out.println("Answer confirmed: Attempting to add flight to DB...");
					break;
				}
			}catch(Exception l){
				System.out.println("Your input is invalid! Please enter 'yes' or 'no': ");
				continue;
			}
		}while(true);
				
				//now getting information to create new flight
				String flightQuery = "INSERT INTO Flight(cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) Values ( ";
				flightQuery += cost + "," + num_sold + "," + num_stops + ", \'" + departure + "\', \'"  + arrival + "\', \'"  + to + "\', \'"  + from + "\');";
				//String cost, num_sold, num_stops, departure, arrival, to, from, plane_id, pilot_id; 
			
				try{
				//System.out.println("test 2");
				System.out.println(flightQuery);
				int rowCount = esql.executeQuery(flightQuery);
				//System.out.println("total row(s): " + rowCount);
			
				}catch(Exception k){
					System.err.println(k.getMessage());

				}
		
			System.out.println("Hello world testing!");
			try{
			
				String flightInfoQuery = "INSERT INTO FlightInfo(pilot_id, plane_id) Values ( \'" + pilot_id + "\', \'" + plane_id + "\');";
			 
				System.out.println(flightInfoQuery);
				int rowCount1 = esql.executeQuery(flightInfoQuery);
				//System.out.println("total row(s): " + rowCount1);
			
			}catch(Exception j){
				System.err.println(j.getMessage());
				
			}
		//}while(true);
		
	}


	public static void AddTechnician(DBproject esql) {//4
		try{
			System.out.print("\t Enter Technician full name: "); 
			String tname;
			do{
				try{
					tname = in.readLine();
					if(tname.length() <=0 || tname.length() > 128){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid! Try again: ");
					continue;
				}
			}while(true);
			
			String query = "INSERT INTO Technician(full_name) VALUES(\'" + tname + "\');";
			System.out.println(query);
			int rowCount = esql.executeQuery(query);
			System.out.println("total row(s): " + rowCount);
			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		try{
			//int flightID;
			String flight_id = "";
			String cust_id = "";
			int temp = 0; 
			
			do{ 
			
				try{
					System.out.print("Enter Customer ID: ");
					cust_id = in.readLine();
					List<List<String>> custQuery = null;
					String custFinder = "SELECT id FROM Customer WHERE id = ";
					
					
					custFinder += cust_id + ";";
					//System.out.println(custFinder);
					custQuery = esql.executeQueryAndReturnResult(custFinder);
					if(esql.executeQuery(custFinder) <= 0){
						System.out.println("This id is not in our database, would you like to sign up as a new guest? Enter 'Y' or 'N': ");
						String newGuest = in.readLine();
						if(!newGuest.equals("Y") && !newGuest.equals("N")){
								System.out.println("Invalid answer. Enter 'Y' or 'N': ");
								return;
						}else if(newGuest.equals("Y")){
							String fname, lname, gtype, dob, address, phone, zipcode;
							
						do{
							try{

							System.out.println("\tEnter first name: ");
							fname = in.readLine();
							if(fname.length() <= 0|| fname.length() > 24){
								throw new RuntimeException();
							}
							break;
							}catch(Exception e){
								System.out.println("Invalid entry");
								continue;
							}
							
						}while(true);
						
						
						do{
							try{

							System.out.println("\tEnter last name: ");
							lname = in.readLine();
							if(lname.length() <= 0 || lname.length() > 24){
								throw new RuntimeException();
							}
							break;
							}catch(Exception e){
								System.out.println("Invalid entry");
								continue;
							}
						}while(true);
						
								
						do{
							try{

							System.out.println("\tGender: ");
							gtype = in.readLine(); 
							if(!gtype.equals("M") && !gtype.equals("F")){
								throw new RuntimeException();
							}
							break;
							}catch(Exception e){
								System.out.println("Invalid entry");
								continue;
							}
						}while(true);
						
						
								
						do{
							try{

							System.out.println("\tDOB(yyyy-MM-dd: ");
							dob = in.readLine();
							String pattern = "yyyy-MM-dd";
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
							simpleDateFormat.setLenient(false);
							Date dob1 = simpleDateFormat.parse(dob);
							
							break;
							}catch(Exception c){
								System.out.println("Invalid Date Format: ");
								continue;
							}
						}while(true);	
						
						
							
						do{
							try{

							System.out.println("\tEnter address: ");
							address = in.readLine();
							if(address.length() <= 0 || address.length() > 256){
								throw new RuntimeException();
							}
							break;
							}catch(Exception e){
								System.out.println("Invalid entry");
								continue;
							}
						}while(true);
							
						do{
							try{

							System.out.println("\tEnter phone ##########: ");
							phone = in.readLine();
							if(phone.length() != 10){
								throw new RuntimeException();
							}
							break;
							}catch(Exception e){
								System.out.println("Invalid entry");
								continue;
							}
						}while(true);
						
							
						do{
							try{

							System.out.println("\tEnter zipcode: ");
							zipcode = in.readLine();
							if(zipcode.length() <= 0 || zipcode.length() > 10){
								throw new RuntimeException();
							}
							break;
							}catch(Exception e){
								System.out.println("Invalid entry");
								continue;
							}
						}while(true);
						
						try{
							String insertGuestQuery = "INSERT INTO Customer(fname, lname, gtype, dob, address, phone, zipcode) Values( '";
							insertGuestQuery += fname + "\' , \'" + lname +  "\' , \'" + gtype + "\' , \'" + dob + "\' , \'" + address + "\' , \'" + phone + "\' , \'" + zipcode + "\');";
							System.out.println(insertGuestQuery);
							int t = esql.executeQuery(insertGuestQuery);
							
							
							break;
						}catch(Exception e){
							System.out.println("Attempting to create new customer...");
							String x = "SELECT MAX(id) as m FROM Customer;";
							
							System.out.println(x);
							List<List<String>> cust_ID = esql.executeQueryAndReturnResult(x);
							cust_id = cust_ID.get(0).get(0); 
							break;
						}	
						}else{
							return;
						}
									
								
					}
					break;
					
					}catch(Exception e){
						System.out.println("This customer does not exist, please enter valid customer ID");
						//System.err.println(e.getMessage());
			
					}
				}while(true);
		
			
			do{			

				try{
					System.out.print("Enter Flight number: ");
					List<List<String>> flightQuery = null;
					String flightFinder = "SELECT cost FROM Flight WHERE fnum = ";
					flight_id = in.readLine();
					flightFinder += flight_id + ";";
					try{
						//System.out.println(flightFinder);
						//String q = 
						flightQuery = esql.executeQueryAndReturnResult(flightFinder);
					}catch(SQLException e){
						System.err.println(e.getMessage());
					}
					String out = "Cost of flight " + flight_id + ": $";
					System.out.print(out);
					String op = flightQuery.get(0).get(0);
					System.out.println(op);
					
					for(List<String> x : flightQuery){
						for(String y : x){
							temp = Integer.parseInt(y);
						}
					}
					if(temp == 0){
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("Your input is invalid! Try again");
					continue;
				}
			}while(true);

			
			
			
				
			String seatsQuery = "SELECT SUM(P.seats - S.num_sold) as avail_seats FROM(SELECT * FROM Flight F, FlightInfo FI WHERE F.fnum = FI.flight_id AND F.fnum = " + flight_id + ") as S, Plane P WHERE S.plane_id = P.id;";
			//System.out.println(seatsQuery);
			List<List<String>> seat_S = esql.executeQueryAndReturnResult(seatsQuery);
			int avail_seats = 0;
			for(List<String> x : seat_S){
				for(String y : x){
					avail_seats = Integer.parseInt(y);
				}
			}
			//String out = "Flight " + flight_id + " has ";
			//System.out.print(out);
			System.out.print(avail_seats);
			System.out.println(" seats remaining");
			
			
			String maxQuery = "(SELECT Max(R.rnum) FROM Reservation R)";
			//System.out.println(maxQuery);
			List<List<String>> maxRes = esql.executeQueryAndReturnResult(maxQuery);
			int max = 0;
			for(List<String> x : maxRes){
				for(String y : x){
					max = Integer.parseInt(y);
				}
			}
			max++;
			
			String qu = "SELECT MAX(id) as m FROM Customer;";
			List<List<String>> maxCus = esql.executeQueryAndReturnResult(qu);
			int t = Integer.parseInt(maxCus.get(0).get(0));
			
			if(avail_seats > 0){
				System.out.println("Flight has available seats. Reserve (Y or N)?");
				String rstatus = "";
				rstatus = in.readLine();
				if(rstatus.equals("Y")){
					String query_sold = "SELECT F.num_sold FROM Flight F WHERE F.fnum = " + flight_id + ";";
					//System.out.print(query_sold);
					List<List<String>> soldNewcount = esql.executeQueryAndReturnResult(query_sold);
					int seatsNew = 0;
					for(List<String> x : soldNewcount){
						for(String y : x){
							seatsNew = Integer.parseInt(y);
						}
					}
					seatsNew++;
				}else{
					return;
				}
				
				String updateQuery = "UPDATE Flight SET num_sold = num_sold + 1 WHERE fnum = " + flight_id + ";";
				String insertResult = "INSERT INTO Reservation(rnum, cid, fid, status) VALUES(" + max + ", " + cust_id + ", " + flight_id + ", \'R\');";
				
				try{
					//System.out.println(updateQuery);
					int a = esql.executeQuery(updateQuery);
				}catch(SQLException e){
					System.err.println(e.getMessage());
				}		
				try{
					//System.out.println(insertResult);
					int b = esql.executeQuery(insertResult);
				}catch(SQLException e){
					System.err.println(e.getMessage());
				}	
			}
			else{
				System.out.println("The flight you are trying to book is full, you have been added to the waitlist");
				String insertResult = "INSERT INTO Reservation(rnum, cid, fid, status) VALUES(" + max + ", " + cust_id + ", " + flight_id + ", \'W\');";
				System.out.println(insertResult);
				int waitResult = esql.executeQuery(insertResult);
				return;
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
			
	}
	

	

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		
            String comma = ",";
            String apos = "'";
            String f_num;
		do{
            try{
				System.out.print("Enter flight number: ");
            	f_num = in.readLine();
            	
            	String testQuery = "SELECT * FROM Flight WHERE fnum = ";
					testQuery += f_num;
					testQuery += ";";
					if(esql.executeQuery(testQuery) < 1){	
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("This flight does not exist, enter valid flight number");
					continue;
				}
				
			}while(true);
			
            try{
			
            String query = "SELECT (seats_available.seats - seats_available.num_sold) AS seats_available FROM (SELECT P.seats, F.num_sold FROM Plane P, Flight F, FlightInfo FI WHERE FI.plane_id = P.id AND F.fnum = FI.flight_id AND F.fnum = ";
            query += f_num;
            query += ") as seats_available;";               
			
			
			//System.out.println(query);
			//System.out.println("\t Number of seats available: ");
			esql.executeQueryAndPrintResult(query);
			//int rowCount = esql.executeQuery(query);
			
			//System.out.println("Available seats: ");
			
			
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		try{
			String query = "(SELECT P.id, COUNT(R.rid) FROM Plane P, Repairs R WHERE P.id = R.plane_id GROUP BY P.id) ORDER BY COUNT(R.rid) DESC"; 
			
			int ans = esql.executeQueryAndPrintResult(query);

		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		try{
			String query = "SELECT EXTRACT(YEAR FROM R.repair_date), COUNT(*) FROM Repairs R GROUP BY(EXTRACT(YEAR FROM R.repair_date)) ORDER BY COUNT(*) ASC";
			int ans = esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		String flight, answer;
		do{
			try{
				System.out.print("\t What flight are you interested in: ");
				flight = in.readLine();
				String testQuery = "SELECT * FROM Flight WHERE fnum = ";
					testQuery += flight;
					testQuery += ";";
					if(esql.executeQuery(testQuery) < 1){	
						throw new RuntimeException();
					}
					break;
				}catch(Exception e){
					System.out.println("This flight does not exist, please enter valid flight");
					continue;
				}
			}while(true);
			
			do{
				try{
					
					System.out.print("\t What status would you like to check? Press 'W' for waitlisted, 'C' for confirmed, and 'R' for Reserved: ");
					//int answer = Integer.parseInt(in.readLine());
					answer = in.readLine();
					if(!answer.equals("W") && !answer.equals("C") && !answer.equals("R")){
						throw new RuntimeException();
					}
					break;
				}catch(Exception E){
					System.out.println("\t Invalid input entered, please enter 'W' 'C' or 'R'!! ");
					continue;
				}
			}while(true);
			
			try{
				
				String query = "(SELECT count(*) FROM Reservation R, Flight F, Customer C WHERE R.fid = F.fnum AND R.cid = C.id AND R.fid = ";
				query += flight;
				query += " AND R.status = '";
				query += answer;
				query += "');";
				
				System.out.println("\t Number of seats available: ");
				int ans = esql.executeQueryAndPrintResult(query);
				//int rowCount = esql.executeQuery(query);
			//if(rowCount <= 0){
				//System.out.println("Invalid flight number entered!!");
			//}
			//System.out.println(query);
			
			//esql.executeQueryAndPrintResult(query);
			
			
				
		}catch(Exception e){
		System.err.println(e.getMessage());
		}
	}
}
