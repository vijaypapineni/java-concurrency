package info.shelfunit.concurrency.venkatsbook.ch008.primes;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

// import info.shelfunit.concurrency.venkatsbook.ch008.PrintResult;

// More imports!
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

import akka.util.Timeout;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.UUID;

// from Programming Concurrency on the JVM by Venkat Subramaniam   

public class UsePrimes {

    public static void main( final String[] args ) throws InterruptedException {
	if ( args.length < 2 ) {
	    System.out.println( "Usage: number numberOfParts" );
	} else {
	    final long start = System.nanoTime();
	    final long count = countPrimes( Integer.parseInt( args[0] ), Integer.parseInt(args[ 1 ]) );
	    final long end = System.nanoTime();
	    System.out.println( "Number of Primes is " + count );
	    System.out.println( "Time taken: " + (end/start)/1.0e9 );
	} // if ( args.length < 2 ) 

    } // end method main

    public static int countPrimes(final int number, final int numberOfParts) {
	ActorSystem system = ActorSystem.create("This-seems-like-a-lot-of-work");
	Timeout timeout = new Timeout( 5 * 1000 );
	final int chunksPerPartition = number / numberOfParts;
	final List< Future< ? > > results = new ArrayList< Future< ? > >();
	for ( int index = 0; index < numberOfParts; index++ ) {
	    final int lower = index * chunksPerPartition + 1;
	    final int upper = (index == numberOfParts - 1) ? number : lower + chunksPerPartition - 1;
	    final List< Integer > bounds = Collections.unmodifiableList( Arrays.asList(lower, upper) );
	    final ActorRef primeFinder = system.actorOf(new Props(Primes.class), UUID.randomUUID().toString());
	    results.add( Patterns.ask(primeFinder, bounds, (30 * 1000) ) );
	} // for ( int index = 0; index < numberOfParts; index++ ) 

	int count = 0;
	for (Future<?> result : results) {

	    try {
		count +=  ( Integer )  Await.result(result, timeout.duration() );
	    } catch ( Exception e ) {
		System.out.println( "Exception in for loop" );
		e.printStackTrace();
	    }

	}
	system.shutdown();
	return count;
    } // countPrimes

} // end UsePrimes
