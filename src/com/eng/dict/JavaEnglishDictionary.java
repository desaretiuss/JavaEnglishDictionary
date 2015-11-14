package com.eng.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class JavaEnglishDictionary {

	private static Multimap<String, String> map = null;
	private static final String LOCAL_DB = "/home/flediymerasi/Public/wordnet-sqlite-31.db";

	public static void main(String[] args) {

		System.out.print("Initializing . . .");

		Connection connection = getDatabaseConnection();

		ResultSet r = getAllData(connection);

		populateMap(r, connection);

		System.out.println(" . . . Done.");

		System.out.println();
		System.out.println("                  ------------------------------------------------------");
		System.out.println("                  |      English Dictionary  - 147.000 unique entries   |");
		System.out.println("                  -------------------------------------------------------");
		System.out.println("          ------------------------------------------------------------------------");
		System.out.println();

		String keyword = getInput();
		processQuery(keyword);

	}

	private static ResultSet getAllData(Connection connection) {

		ResultSet resultSet = null;
		String sql = " SELECT `lemma`, `definition`" + " FROM `words` NATURAL JOIN"
				+ " `senses` NATURAL JOIN `synsets`";

		try {
			Statement statement = connection.createStatement();

			resultSet = statement.executeQuery(sql);

		} catch (SQLException s) {
			s.printStackTrace();
		}
		return resultSet;
	}

	private static void populateMap(ResultSet resultSet, Connection connection) {

		map = ArrayListMultimap.create();

		try {
			while (resultSet.next()) {

				String word = resultSet.getString("lemma");
				String definition = resultSet.getString("definition");

				map.put(word, definition);

			}
		} catch (SQLException s) {
			s.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException s) {
				s.printStackTrace();
			}
		}
	}

	private static String getInput() {

		String keyword = null;
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("\n Type a word or phrase: ");
			keyword = bufferedReader.readLine().toLowerCase();
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return keyword;
	}

	private static boolean isInputValid(String keyword) {

		if (keyword.matches("[^\\x00-\\x7F]+")) {

			System.out.println("\n The query must contain only english letters.");
			return false;

		}

		return true;
	}

	private static Collection<String> lookupWord(String key) {

		Collection<String> result = map.get(key);

		return result;

	}

	private static void printResult(Collection<String> result) {

		Iterator<String> i = result.iterator();

		int j = 1; // Count the definitions
		while (i.hasNext()) {

			String definition = (String) i.next();
			System.out.println(" " + j + ". " + definition);
			++j;
		}

	}

	private static String searchSimiliarResults(String keyword) {

		String similiarResults = "";

		for (String key : map.keySet()) {

			if (key.startsWith(keyword)) {

				similiarResults += key + ", ";

			}

		}

		return similiarResults;
	}

	private static void printSimiliarResults(String similiarResults) {

		String[] sim_array = similiarResults.split("\\,");

		int j = 1;
		for (int i = 0; i < sim_array.length; i++) {
			++j;
			System.out.printf("%-30.30s", " -" + sim_array[i]);

			if (j % 3 == 0)
				System.out.println();
		}

	}

	public static void processQuery(String keyword) {

		if (isInputValid(keyword) == true)

			if (lookupWord(keyword).isEmpty()) {

				System.out.println(" Sorry, no exact matches for " + "\"" + keyword +"\"" + ".");

				if (!searchSimiliarResults(keyword).isEmpty()) {
					String r = searchSimiliarResults(keyword);
					System.out.println(" The nearest results: \n");
					printSimiliarResults(r);
				}

				System.out.println();

				// Prompt user for another word
				keyword = getInput();
				processQuery(keyword);

			} else {
				printResult(lookupWord(keyword));

				keyword = getInput();
				processQuery(keyword);
			}

	}

	private static Connection getDatabaseConnection() {

		Connection connection = null;
		String DB_URL = "jdbc:sqlite:" + LOCAL_DB;

		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection(DB_URL);

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return connection;
	}

}// end class