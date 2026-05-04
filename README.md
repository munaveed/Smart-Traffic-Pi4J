# Smart-Traffic-Pi4J
A Multithreaded Traffic Control System with Emergency Preemption using Java and Raspberry Pi

This project demonstrates the practical application of core Operating System concepts specifically multithreading, thread synchronization, and priority scheduling through a hardware based Smart Traffic Light Controller. Using a Raspberry Pi 4 and the Pi4J library, our team developed a system that manages three concurrent threads: a standard traffic loop, a pedestrian crossing monitor, and a high priority emergency vehicle override. To ensure safety and prevent race conditions, we implemented a binary semaphore as a mutex, creating a "critical section" that protects shared GPIO resources and ensures mutual exclusion during state transitions. By utilizing preemptive priority scheduling (assigning MAX_PRIORITY to the emergency thread), the system can instantaneously halt normal traffic flow to execute a higher priority safety routine, effectively simulating a real-world smart intersection

https://github.com/user-attachments/assets/6ce34d08-d1e4-4de1-8a49-299705c032df

This prototype demonstrates a real-time traffic controller where Safety > Flow. By implementing Preemptive Priority Scheduling, the system ensures that emergency overrides are handled with zero-latency, while a Binary Semaphore maintains hardware integrity across four concurrent threads. This project successfully bridges the gap between high level Java multithreading and low-level embedded hardware.

---For a deep dive into the System Architecture, Semaphore Implementation, and Priority Scheduling logic, read the full final project report in my docs folder

Quick Technical Specs
Language: Java 17   
Hardware: Raspberry Pi 4, LEDs, Pushbuttons, 220Ω Resistors   
OS Logic: * Traffic Thread: NORM_PRIORITY   
Emergency Thread: MAX_PRIORITY   
Synchronization: Binary Semaphore (lightMutex) 


