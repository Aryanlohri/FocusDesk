# 📋 Personal Task Manager

A desktop application built in Java (Swing) that helps students and professionals manage tasks with deadlines, priorities, and automatic deadline alerts — solving the very real problem of missed deadlines due to poor task visibility.

---

## 🖼️ Features

| Feature | Description |
|---|---|
| Add / Edit / Delete Tasks | Full CRUD operations with a form dialog |
| Priority Levels | HIGH, MEDIUM, LOW — colour-coded in the table |
| Deadline Tracking | Tasks turn **orange** when due soon, **red** when overdue |
| Recurring Tasks | Tasks that automatically reschedule after completion |
| Deadline Alerts | Background thread checks every 60 s and pops a warning |
| Filter & Sort | Filter by status; sort by priority or deadline |
| Persistent Storage | Tasks saved to a plain-text file — survive app restarts |

---

## 🗂️ Project Structure

```
TaskManager/
├── src/
│   └── taskmanager/
│       ├── Main.java                    ← Entry point
│       ├── model/
│       │   ├── Task.java                ← Base task class (OOP)
│       │   └── RecurringTask.java       ← Subclass (Inheritance)
│       ├── service/
│       │   ├── TaskService.java         ← Business logic / CRUD
│       │   └── DeadlineMonitor.java     ← Background thread (Multithreading)
│       ├── ui/
│       │   ├── MainWindow.java          ← Main JFrame
│       │   ├── TaskFormDialog.java      ← Add/Edit dialog
│       │   ├── TaskTableModel.java      ← JTable data model
│       │   └── TaskTableRenderer.java  ← Colour-coded rows
│       └── util/
│           ├── TaskException.java       ← Custom exceptions
│           └── FileHandler.java         ← File I/O persistence
├── data/
│   └── tasks.dat                        ← Auto-created on first run
├── compile.sh                           ← Linux/macOS build script
├── compile.bat                          ← Windows build script
└── README.md
```

---

## ✅ Prerequisites

- **Java JDK 17 or higher** (uses switch expressions and records)
- No external libraries — only the Java Standard Library

Verify your installation:

```bash
java -version
javac -version
```

Both should report version 17 or above.

---

## 🚀 How to Run

### Option 1 — Linux / macOS

```bash
# 1. Clone or download the project
git clone https://github.com/YOUR_USERNAME/personal-task-manager.git
cd personal-task-manager

# 2. Make the script executable (one-time step)
chmod +x compile.sh

# 3. Compile and run
./compile.sh run
```

### Option 2 — Windows

```bat
REM Open Command Prompt in the project folder, then:
compile.bat run
```

### Option 3 — Manual (any OS)

```bash
# Compile
mkdir out
find src -name "*.java" | xargs javac -d out      # Linux/macOS
# OR on Windows:
dir /s /b src\*.java > out\sources.txt && javac -d out @out\sources.txt

# Run (from project root — important for the data/ path)
java -cp out taskmanager.Main
```

### Option 4 — IntelliJ IDEA / Eclipse

1. Open the project root as an existing project
2. Mark `src/` as the **Sources Root**
3. Set `taskmanager.Main` as the run configuration's main class
4. Run the project from the IDE

> **Important:** Always run from the project root directory so that `data/tasks.dat` is created in the right place.

---

## 🎮 How to Use

### Adding a Task
1. Click **➕ Add Task** in the toolbar
2. Fill in the title (required), description, priority, and deadline (format: `dd-MM-yyyy`)
3. Tick **Recurring task** and set an interval if it repeats
4. Click **Save**

### Editing a Task
- Select a row and click **✏️ Edit**, OR double-click the row

### Deleting a Task
- Select a row and click **🗑️ Delete**; confirm the dialog

### Marking Complete
- Select a row and click **✅ Mark Done**
- For recurring tasks, the deadline advances automatically instead of marking complete

### Filtering
- Use the **Filter** dropdown to show: All / Pending / In Progress / Completed / Overdue / Due Soon

### Sorting
- **Sort: Priority** — HIGH tasks rise to the top
- **Sort: Deadline** — nearest deadline first

### Deadline Alerts
- The app checks every **60 seconds** in the background
- A warning popup appears automatically for tasks that are overdue or due within 2 days

---

## 🎨 Colour Legend

| Row colour | Meaning |
|---|---|
| 🔴 Red | Overdue (deadline passed, not completed) |
| 🟠 Orange | Due soon (today or within 2 days) |
| 🟢 Green | Completed |
| Bold text | HIGH priority task |

---

## 💾 Data File

Tasks are stored in `data/tasks.dat` (created automatically). The file uses a pipe-delimited format:

```
# Normal task
id|title|description|priority|status|deadline

# Recurring task
R|id|title|description|priority|status|deadline|repeatEveryDays
```

You can back this file up or copy it to another machine to transfer your tasks.

---

## 🔧 Java Concepts Demonstrated

| Concept | Where |
|---|---|
| OOP — Encapsulation | `Task.java` — private fields, getters/setters |
| OOP — Inheritance | `RecurringTask extends Task` |
| OOP — Polymorphism | `toCsv()` overridden in `RecurringTask` |
| Custom Exceptions | `TaskException` with 3 subclasses |
| Exception Handling | try-catch in `FileHandler`, `TaskService`, UI layers |
| Multithreading | `DeadlineMonitor` runs as a daemon thread |
| Thread safety | `SwingUtilities.invokeLater()` for UI updates from threads |
| File I/O | `BufferedReader` / `BufferedWriter` in `FileHandler` |
| Collections | `ArrayList`, `stream()`, `Comparator` in `TaskService` |
| Swing GUI | `JFrame`, `JTable`, `JDialog`, `JToolBar`, custom `TableModel` |

---

## 🐛 Troubleshooting

| Problem | Solution |
|---|---|
| `javac: command not found` | Install JDK 17+ and add it to your PATH |
| App opens but data not saved | Make sure you run from the project root, not from `src/` or `out/` |
| Date validation error | Use the exact format `dd-MM-yyyy`, e.g. `25-12-2025` |
| Alert popup doesn't appear | It fires every 60 s — add a task with today's deadline and wait |

---

## 📄 License

This project is submitted as an academic assignment and is not licensed for commercial use.
