package com.consorciosenred.ada.connect;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400ConnectionPool;
import com.ibm.as400.access.CommandCall;

public class GetConnectionPoolAS400 {

	/**
	 * @param args
	 */
	public static void main(String[] parameters) {
		String system = parameters[0];
		String userId = parameters[1];
		String password = parameters[2];

		try {
			// Create an AS400ConnectionPool.
			AS400ConnectionPool testPool = new AS400ConnectionPool();

			testPool.setMaxConnections(128); // Set a maximum of 128 connections
			// to this pool.

			// Set a maximum lifetime for 30 minutes for connections.
			testPool.setMaxLifetime(1000 * 60 * 30); // 30 min Max lifetime
			// since created.

			// Preconnect 5 connections to the AS400.COMMAND service.
			testPool.fill(system, userId, password, AS400.COMMAND, 1);
			System.out.println();
			System.out.println("Preconnected 1 connection to the AS400.COMMAND service");

			// Call getActiveConnectionCount and getAvailableConnectionCount to
			// see how many
			// connections are in use and available for a particular system.
			System.out.println("Number of active connections: " + testPool.getActiveConnectionCount(system, userId));
			System.out.println("Number of available connections for use: "
					+ testPool.getAvailableConnectionCount(system, userId));

			// Create a connection to the AS400.COMMAND service. (Use the
			// service number
			// constants defined in the AS400 class (FILE,
			// DATAQUEUE, and so on.))
			// Since connections have already been filled, the usual time spent
			// connecting
			// to the command service is avoided.
			AS400 newConn1 = testPool.getConnection(system, userId, password, AS400.COMMAND);

			System.out.println();
			System.out.println("getConnection gives out an existing connection to user");
			System.out.println("Number of active connections: " + testPool.getActiveConnectionCount(system, userId));
			System.out.println("Number of available connections for use:  "
					+ testPool.getAvailableConnectionCount(system, userId));

			// Create a new command call object and run a command.
			CommandCall cmd1 = new CommandCall(newConn1);
			cmd1.run("SNDMSG MSG('hola FECHU') TOUSR(INF1FER2)");

			// Return the connection to the pool.
			testPool.returnConnectionToPool(newConn1);

			System.out.println();
			System.out.println("Returned a connection to pool");
			System.out.println("Number of active connections: " + testPool.getActiveConnectionCount(system, userId));
			System.out.println("Number of available connections for reuse: "
					+ testPool.getAvailableConnectionCount(system, userId));

			// Create a connection to the AS400.COMMAND service. This will
			// return the same
			// object as above for reuse.
			@SuppressWarnings("unused")
			AS400 newConn2 = testPool.getConnection(system, userId, password, AS400.COMMAND);

			System.out.println();
			System.out.println("getConnection gives out an existing connection to user");
			System.out.println("Number of active connections: " + testPool.getActiveConnectionCount(system, userId));
			System.out.println("Number of available connections for reuse: "
					+ testPool.getAvailableConnectionCount(system, userId));

			// Create a connection to the AS400.COMMAND service. This will
			// create a new
			// connection as there are not any connections in the pool to reuse.
			@SuppressWarnings("unused")
			AS400 newConn3 = testPool.getConnection(system, userId, password, AS400.COMMAND);

			System.out.println();
			System.out.println("getConnection creates a new connection because there are no connections available");
			System.out.println("Number of active connections: " + testPool.getActiveConnectionCount(system, userId));
			System.out.println("Number of available connections for reuse: "
					+ testPool.getAvailableConnectionCount(system, userId));

			// Close the test pool.
			testPool.close();
		} catch (Exception e) {
			System.out.println("Pool operations failed");
			System.out.println(e);
			e.printStackTrace();
		}

	}

}
