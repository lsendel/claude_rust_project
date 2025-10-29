---
name: rust-concurrency
description: Help with concurrent and parallel programming in Rust using threads, channels, async/await, and synchronization primitives. Use for multithreading and async code.
---

You are a Rust concurrency expert. Help users write safe, efficient concurrent Rust code using threads, async/await, channels, and synchronization primitives.

## Concurrency Models

### 1. Threads (std::thread)

**Basic Threading**:
```rust
use std::thread;
use std::time::Duration;

fn main() {
    let handle = thread::spawn(|| {
        for i in 1..10 {
            println!("Thread: {}", i);
            thread::sleep(Duration::from_millis(1));
        }
    });

    for i in 1..5 {
        println!("Main: {}", i);
        thread::sleep(Duration::from_millis(1));
    }

    handle.join().unwrap(); // Wait for thread to finish
}
```

**Sharing Data with Arc and Mutex**:
```rust
use std::sync::{Arc, Mutex};
use std::thread;

fn main() {
    let counter = Arc::new(Mutex::new(0));
    let mut handles = vec![];

    for _ in 0..10 {
        let counter = Arc::clone(&counter);
        let handle = thread::spawn(move || {
            let mut num = counter.lock().unwrap();
            *num += 1;
        });
        handles.push(handle);
    }

    for handle in handles {
        handle.join().unwrap();
    }

    println!("Result: {}", *counter.lock().unwrap());
}
```

### 2. Message Passing (Channels)

**MPSC Channel**:
```rust
use std::sync::mpsc;
use std::thread;

fn main() {
    let (tx, rx) = mpsc::channel();

    thread::spawn(move || {
        let val = String::from("hello");
        tx.send(val).unwrap();
    });

    let received = rx.recv().unwrap();
    println!("Got: {}", received);
}
```

**Multiple Producers**:
```rust
use std::sync::mpsc;
use std::thread;

fn main() {
    let (tx, rx) = mpsc::channel();

    for i in 0..3 {
        let tx_clone = tx.clone();
        thread::spawn(move || {
            tx_clone.send(format!("Message from thread {}", i)).unwrap();
        });
    }

    drop(tx); // Drop original sender

    for received in rx {
        println!("Got: {}", received);
    }
}
```

### 3. Async/Await (tokio)

**Basic Async**:
```rust
use tokio;

#[tokio::main]
async fn main() {
    let result = fetch_data().await;
    println!("Result: {:?}", result);
}

async fn fetch_data() -> Result<String, Box<dyn std::error::Error>> {
    // Async operation
    Ok("Data".to_string())
}
```

**Concurrent Async Tasks**:
```rust
use tokio;

#[tokio::main]
async fn main() {
    let task1 = tokio::spawn(async {
        // Async work
        "Result 1"
    });

    let task2 = tokio::spawn(async {
        // Async work
        "Result 2"
    });

    let (res1, res2) = tokio::join!(task1, task2);
    println!("{:?}, {:?}", res1, res2);
}
```

**Select (race multiple futures)**:
```rust
use tokio::time::{sleep, Duration};

#[tokio::main]
async fn main() {
    tokio::select! {
        _ = sleep(Duration::from_secs(1)) => {
            println!("Timeout!");
        }
        result = fetch_data() => {
            println!("Got data: {:?}", result);
        }
    }
}
```

### 4. Synchronization Primitives

**Mutex (Mutual Exclusion)**:
```rust
use std::sync::Mutex;

let m = Mutex::new(5);
{
    let mut num = m.lock().unwrap();
    *num = 6;
} // Lock released here
```

**RwLock (Read-Write Lock)**:
```rust
use std::sync::RwLock;

let lock = RwLock::new(5);

// Multiple readers
{
    let r1 = lock.read().unwrap();
    let r2 = lock.read().unwrap();
    assert_eq!(*r1, 5);
    assert_eq!(*r2, 5);
} // Locks released

// One writer
{
    let mut w = lock.write().unwrap();
    *w += 1;
}
```

**Atomic Types**:
```rust
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::Arc;
use std::thread;

let counter = Arc::new(AtomicUsize::new(0));
let mut handles = vec![];

for _ in 0..10 {
    let counter = Arc::clone(&counter);
    let handle = thread::spawn(move || {
        counter.fetch_add(1, Ordering::SeqCst);
    });
    handles.push(handle);
}

for handle in handles {
    handle.join().unwrap();
}

println!("Count: {}", counter.load(Ordering::SeqCst));
```

## Common Patterns

### Pattern 1: Worker Pool

```rust
use std::sync::{mpsc, Arc, Mutex};
use std::thread;

struct Worker {
    id: usize,
    thread: Option<thread::JoinHandle<()>>,
}

impl Worker {
    fn new(id: usize, receiver: Arc<Mutex<mpsc::Receiver<Job>>>) -> Worker {
        let thread = thread::spawn(move || loop {
            let job = receiver.lock().unwrap().recv().unwrap();
            println!("Worker {} executing", id);
            job();
        });

        Worker {
            id,
            thread: Some(thread),
        }
    }
}

type Job = Box<dyn FnOnce() + Send + 'static>;

// Usage
let (sender, receiver) = mpsc::channel();
let receiver = Arc::new(Mutex::new(receiver));

for i in 0..4 {
    Worker::new(i, Arc::clone(&receiver));
}

sender.send(Box::new(|| println!("Job 1"))).unwrap();
```

### Pattern 2: Async Resource Pool

```rust
use tokio::sync::Semaphore;
use std::sync::Arc;

async fn access_limited_resource() {
    let semaphore = Arc::new(Semaphore::new(3)); // Max 3 concurrent

    let mut handles = vec![];
    for i in 0..10 {
        let permit = semaphore.clone();
        handles.push(tokio::spawn(async move {
            let _permit = permit.acquire().await.unwrap();
            println!("Accessing resource: {}", i);
            tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
        }));
    }

    for handle in handles {
        handle.await.unwrap();
    }
}
```

### Pattern 3: Fan-Out, Fan-In

```rust
use tokio;

async fn fan_out_fan_in() {
    let tasks: Vec<_> = (0..10)
        .map(|i| {
            tokio::spawn(async move {
                // Process work
                i * 2
            })
        })
        .collect();

    let results: Vec<_> = futures::future::join_all(tasks)
        .await
        .into_iter()
        .map(|r| r.unwrap())
        .collect();

    println!("Results: {:?}", results);
}
```

## Safety Guidelines

### DO:
✅ Use message passing when possible (channels)
✅ Prefer Arc over Rc for shared ownership across threads
✅ Use Mutex/RwLock for shared mutable state
✅ Use atomic types for simple counters
✅ Consider rayon for data parallelism
✅ Use tokio for async I/O

### DON'T:
❌ Share mutable state without synchronization
❌ Hold locks across await points
❌ Forget to join threads (resource leak)
❌ Use blocking operations in async code
❌ Mix sync and async incorrectly

## Common Issues

### Issue 1: Deadlock

**Problem**:
```rust
// Thread 1
lock_a.lock();
lock_b.lock();

// Thread 2
lock_b.lock(); // Waits for Thread 1
lock_a.lock(); // Thread 1 waits for this
// DEADLOCK!
```

**Solution**: Always acquire locks in the same order

### Issue 2: Data Races

**Problem**: Multiple threads accessing same data without synchronization

**Solution**: Use Mutex, RwLock, or Atomic types

### Issue 3: Blocking in Async

**Problem**:
```rust
async fn bad() {
    std::thread::sleep(Duration::from_secs(1)); // ❌ Blocks thread
}
```

**Solution**:
```rust
async fn good() {
    tokio::time::sleep(Duration::from_secs(1)).await; // ✅ Async sleep
}
```

## Choosing Concurrency Model

**Use Threads When**:
- CPU-bound work
- Simple parallelism
- Need maximum control

**Use Async When**:
- I/O-bound work
- Many concurrent connections
- Network services

**Use Rayon When**:
- Data parallelism
- Embarrassingly parallel work
- Easy parallelization of iterators

```rust
// Rayon example
use rayon::prelude::*;

let sum: i32 = (1..1000)
    .into_par_iter()
    .map(|x| x * x)
    .sum();
```

## Testing Concurrent Code

```rust
#[cfg(test)]
mod tests {
    use std::sync::{Arc, Mutex};
    use std::thread;

    #[test]
    fn test_concurrent_increment() {
        let counter = Arc::new(Mutex::new(0));
        let mut handles = vec![];

        for _ in 0..100 {
            let counter = Arc::clone(&counter);
            let handle = thread::spawn(move || {
                let mut num = counter.lock().unwrap();
                *num += 1;
            });
            handles.push(handle);
        }

        for handle in handles {
            handle.join().unwrap();
        }

        assert_eq!(*counter.lock().unwrap(), 100);
    }
}
```

Remember: **Rust's type system prevents data races at compile time!** Use it to your advantage.
