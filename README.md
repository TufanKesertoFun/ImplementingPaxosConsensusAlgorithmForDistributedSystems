
# Paxos Council Election - Distributed Systems Assignment

## Overview

This project implements a full Paxos-based distributed leader election system for a simulated “Council of Members” (M1–M9). Each member is an independent Java process communicating through TCP sockets. The system supports multiple network conditions, concurrent proposals, proposer failures, and ensures that Paxos safety and liveness properties are maintained across all scenarios.

---

# 1. Architecture

### **Key Components**

| Component | Description |
|----------|-------------|
| **CouncilMember** | Main entry point for each node (M1–M9). Loads config, starts transport, initializes roles, and optionally triggers proposals. |
| **Transport Layer (TCP)** | Each member has a TCP server and sends messages using a TCP client. |
| **Roles Implemented** | Fully implemented Proposer, Acceptor, and Learner roles following Leslie Lamport’s Paxos algorithm. |
| **Network Profiles** | Simulated profiles: `reliable`, `standard`, `latent`, and `failure`. These introduce delay, drops, or both. |
| **MessageRouter** | Central router for PREPARE, PROMISE, ACCEPT_REQUEST, ACCEPTED, DECIDE, and NACK messages. |

---

# 2. Paxos Workflow

### **Full Paxos Message Flow**
```
PREPARE → PROMISE → ACCEPT_REQUEST → ACCEPTED → DECIDE (Consensus)
```

### **Safety**
- Only one value may be chosen.
- If a value is chosen, all future proposals must preserve it.

### **Liveness**
- In stable conditions, a proposer with the highest proposal number eventually succeeds.

---

# 3. Running the System

## Requirements
- Java 17+
- Maven
- PowerShell (for scenario scripts)
- Windows OS (scripts launch multiple terminals)

## Build:
```
mvn -q clean package
```

---

# 4. Scenario Scripts

The project includes **three major test scenarios**, each matching the assignment rubric.

---

# Scenario 1 – Ideal Network (Reliable)

### **Description**
All nodes run under the `reliable` profile (no drops, no latency).  
M4 proposes candidate **M5**.

### **Expected Result**
- Fast consensus
- All nodes log:  
  `CONSENSUS: v=M5 has been elected Council President!`

### **Run**
```
.\scenario1.ps1
```

---

# Scenario 2 – Concurrent Proposals (Reliable)

### **Description**
- M4 proposes **M5** on startup.
- M9 proposes **M7** on startup.
- Both PREPARE concurrently → Paxos resolves via highest proposal number.

### **Expected Result**
A single final consensus on either M5 or M7.

### **Run**
```
.\scenario2.ps1
```

---

# Scenario 3 – Mixed Profiles & Failure Handling

This is the most advanced test and demonstrates the robustness of Paxos under heterogeneous and failing conditions.

## Network Profiles
| Member | Profile |
|--------|---------|
| M1 | reliable |
| M2 | latent |
| M3 | failure |
| M4–M9 | standard |

---

## **Scenario 3a – Standard Proposer (M4)**

M4 proposes **M5** under mixed network conditions.

### Expected:
- Despite latency/failure patterns, consensus is reached.

### Run:
```
.\scenario3a.ps1
```

---

## **Scenario 3b – Latent Proposer (M2)**

M2 proposes **M6** (with heavy latency).

### Observed:
- PROMISEs arrive after large delays  
- Consensus eventually reached  
- DECIDE received by all non-failing nodes

### Run:
```
.\scenario3b.ps1
```

---

## **Scenario 3c – Failing Proposer (M3)**

### Steps:
1. M3 (failure profile) auto-proposes **M5**.  
2. M3 sends PREPARE + ACCEPT_REQUEST.  
3. Script **kills M3** after 4 seconds (simulating crash).  
4. M4 auto-proposes **M7** and completes consensus.

### Expected:
- M3 does **not** reach consensus (crashed).
- M4's proposal wins.
- All healthy nodes log:  
  `CONSENSUS: v=M7 has been elected Council President!`

### Run:
```
.\scenario3c.ps1
```

---

# 5. Why PREPARE Appears After CONSENSUS (Important)

In mixed-profile scenarios, some nodes (especially M3) continue sending:
```
PREPARE n=XX
```
**even after consensus has been reached**.

This is normal because:

1. M3 is configured with `failure` profile → high drop + retry behaviour.
2. NACK handler in proposer triggers repeated PREPARE retries.
3. Paxos safety guarantees that once the value is chosen, **it cannot change**, even if PREPARE spam continues.
4. Asynchronous logs do not reflect global ordering.

This demonstrates **correct Paxos behavior under failure**.

---

# 6. Why M3 Does Not Log CONSENSUS in Scenario 3c

M3 is:
- A **failing** node
- Spamming retries
- **Terminated** by the script before DECIDE reaches it

A failed node missing the final consensus is **expected and correct**.

The requirement:
> The remaining operational nodes should still reach consensus  
is satisfied.

---

# 7. Config File Format

`network.config`:
```
M1,localhost,9001
M2,localhost,9002
...
M9,localhost,9009
```

---

# 8. Troubleshooting

### If automatic proposals don’t trigger consensus:
Delay in CouncilMember is needed:
```java
Thread.sleep(2000);
```
before auto-propose.

### If terminal title errors appear:
Replace:
```
$host.UI.RawUI.WindowTitle
```
with:
```
[Console]::Title = 'Mx'
```

### If logs don’t show consensus:
Check:
- Network profile packet drop rate
- Whether proposer starts too early
- Whether nodes have port conflicts

---

# 9. Summary

This project fully satisfies the requirements of the distributed systems Paxos assignment:

- Full proposer/acceptor/learner implementation  
- Message-based TCP communication  
- Mixed network delays and drops  
- Multiple simultaneous proposals  
- Graceful recovery after proposer failure  
- Clear logging of the Paxos phases  
- Demonstrated consensus across all scenarios

---

# Author

**Mustafa Tufan Keser**  
Master of Computer Science  
University of Adelaide  
2025

---
