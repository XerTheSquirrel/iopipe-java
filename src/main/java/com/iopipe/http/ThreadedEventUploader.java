package com.iopipe.http;

import com.iopipe.IOpipeEventUploader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This is an event uploader which
 *
 * @since 2018/07/23
 */
public final class ThreadedEventUploader
	implements IOpipeEventUploader
{
	/** The runner thread. */
	protected final Thread _thread;
	
	/** The runner itself. */
	protected final __Runner__ _runner;
	
	/**
	 * Initializes the threaded event uploader.
	 *
	 * @param __con The connection to use.
	 * @throws NullPointerException On null arguments.
	 * @since 2018/07/23
	 */
	public ThreadedEventUploader(RemoteConnection __con)
		throws NullPointerException
	{
		if (__con == null)
			throw new NullPointerException();
		
		// Setup runner
		__Runner__ runner = new __Runner__(__con);
		
		// Then setup the thread running that
		Thread thread = new Thread(runner, "IOpipe-EventUploader");
		thread.setDaemon(true);
		thread.start();
		
		this._runner = runner;
		this._thread = thread;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/07/23
	 */
	@Override
	public final void await()
	{
		this._runner.__await();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/07/23
	 */
	@Override
	public final int badRequestCount()
	{
		return this._runner._badrequestcount.get();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2018/07/23
	 */
	@Override
	public final void upload(RemoteRequest __r)
		throws NullPointerException
	{
		this._runner.__upload(__r);
	}
	
	/**
	 * This class is the runner for background event uploads, it waits for
	 * things in the queue and tries to upload them.
	 *
	 * @since 2018/07/23
	 */
	private static final class __Runner__
		implements Runnable
	{
		/** The number of uploads to batch together. */
		private static final int _BATCH_COUNT =
			64;
		
		/** The limit to the batch count. */
		private static final int _BATCH_LIMIT =
			_BATCH_COUNT - 1;
		
		/** The connection that is used. */
		protected final RemoteConnection connection;
		
		/** Use concurrent queue since it is quite fast. */
		final Queue<RemoteRequest> _queue =
			new ConcurrentLinkedQueue<>();
		
		/** The number of bad requests. */
		final AtomicInteger _badrequestcount =
			new AtomicInteger();
		
		/** The number of running lambdas. */
		final AtomicInteger _activecount =
			new AtomicInteger();
		
		/** The number of entries in the queue. */
		final AtomicInteger _inqueue =
			new AtomicInteger();
		
		/** Lock for the event queue. */
		final Lock _queuelock =
			new ReentrantLock();
		
		/** Condition which triggers when something is added. */
		final Condition _queuetrigger =
			this._queuelock.newCondition();
		
		/** The thread which is considered the last thread of execution. */
		final AtomicReference<Thread> _finalthread =
			new AtomicReference<>();
		
		/** Counter index for the final thread of execution. */
		final AtomicInteger _finalthreadcounter =
			new AtomicInteger(1);
		
		/**
		 * Initializes the runner.
		 *
		 * @param __con The connection used.
		 * @throws NullPointerException On null arguments.
		 * @since 2018/07/23
		 */
		private __Runner__(RemoteConnection __con)
		{
			if (__con == null)
				throw new NullPointerException();
			
			this.connection = __con;
		}
		
		/**
		 * {@inheritDoc}
		 * @since 2018/07/23
		 */
		@Override
		public final void run()
		{
			AtomicInteger badrequestcount = this._badrequestcount;
			AtomicInteger activecount = this._activecount;
			AtomicInteger inqueue = this._inqueue;
			Queue<RemoteRequest> queue = this._queue;
			Lock queuelock = this._queuelock;
			Condition queuetrigger = this._queuetrigger;
			AtomicInteger finalthreadcounter = this._finalthreadcounter;
			
			// Batch multiple requests from the queue to reduce locking that
			// is done
			RemoteRequest[] batch = new RemoteRequest[_BATCH_COUNT];
			
			// Constantly read input events
			for (;;)
			{
				// Determine how many items are waiting in the queue and
				// can safely be read
				int awaiting = inqueue.getAndSet(0);
				
				// The queue is empty so just wait until it fills again
				if (awaiting == 0)
				{
					queuelock.lock();
					try
					{
						// Just check back every second
						queuetrigger.awaitNanos(1_000_000_000L);
					}
					
					// Ignore this and just try the loop again
					catch (InterruptedException e)
					{
					}
					
					// Always clear the lock
					finally
					{
						queuelock.unlock();
					}
					
					// Try again
					continue;
				}
				
				// Pull items from the queue
				int count = 0;
				for (int i = 0; i < awaiting; i++)
				{
					RemoteRequest request = queue.poll();
					
					if (i < _BATCH_COUNT)
						batch[count++] = request;
				}
				
				// Send reports in batches
				for (int i = 0; i < count; i++)
					try
					{
						// Send it
						RemoteResult result = this.connection.send(
							RequestType.POST, batch[i]);
						
						// Clear so the reference gets garbage collected
						batch[i] = null;
						
						// Only the 200 range is valid for okay responses
						int code = result.code();
						if (!(code >= 200 && code < 300))
							badrequestcount.getAndIncrement();
					}
					
					// Failed to write to the server
					catch (RemoteException e)
					{
						badrequestcount.getAndIncrement();
					}
				
				// If this was the last invocation then notify the running
				// thread that this happened
				if (activecount.decrementAndGet() == 0)
					finalthreadcounter.incrementAndGet();
			}
		}
		
		/**
		 * Increases the number of invocations that are currently happening
		 * so that way events in the background can be sent without waiting
		 * for the request to generate a result.
		 *
		 * @since 2018/07/23
		 */
		public final void __await()
		{
			this._activecount.getAndIncrement();
		}
		
		/**
		 * Uploads the specified event into the queue and either returns
		 * immedietly if there are multiple invocations running or blocks until
		 * there is only one left running.
		 *
		 * @param __r The request to upload.
		 * @throws NullPointerException On null arguments.
		 * @since 2018/07/23
		 */
		public final void __upload(RemoteRequest __r)
			throws NullPointerException
		{
			if (__r == null)
				throw new NullPointerException();
			
			// Try and become the final thread
			AtomicReference<Thread> finalthread = this._finalthread;
			boolean isfinalthread = this._finalthread.compareAndSet(null,
				Thread.currentThread());
			
			// This is used to determine if the last invocation was triggered
			AtomicInteger finalthreadcounter = this._finalthreadcounter;
			int finalcounter = 0;
			if (isfinalthread)
				finalcounter = finalthreadcounter.get();
			
			// Add to the queue
			Queue<RemoteRequest> queue = this._queue;
			queue.add(__r);
			
			// Signal thread that an event was pushed, but only if there was
			// nothing (the other thread would have been asleep)
			int was = this._inqueue.getAndIncrement();
			if (was == 0)
			{
				Lock queuelock = this._queuelock;
				queuelock.lock();
				try
				{
					this._queuetrigger.signal();
				}
				finally
				{
					queuelock.unlock();
				}
			}
			
			// If this is the final thread then we have to wait until all the
			// events have been drained before we continue
			if (isfinalthread)
				while (finalcounter == finalthreadcounter.get())
					continue;
		}
	}
}

