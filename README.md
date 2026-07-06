# Smart Library Circulation & Automation System (SLCAS)

A Java Swing application for MIVA Open University demonstrating OOP, data structures, algorithms, recursion, and event-driven GUI programming.

## Project Structure

```
src/
├── model/          LibraryItem (abstract), Book, Magazine, Journal, Borrowable, UserAccount, LibraryDatabase
├── controller/     LibraryManager, SearchEngine, BorrowController
├── gui/            MainWindow, ViewItemsPanel, BorrowPanel, AdminPanel, SearchSortPanel, ReportsPanel
└── utils/          IDGenerator, FileHandler, SortAlgorithms, ItemCache
```

## Requirements Coverage

| Requirement | Implementation |
|---|---|
| OOP hierarchy | `LibraryItem` abstract class, `Book`/`Magazine`/`Journal` subclasses, `Borrowable` interface |
| Polymorphism | `LibraryManager.processItem()` handles any `LibraryItem` type |
| ArrayList | Stores all library items in `LibraryDatabase` |
| Queue | Reservation/waitlist in `BorrowController` |
| Stack | Undo stack for admin operations in `LibraryDatabase` |
| Array cache | `ItemCache` — fixed-size array for most accessed items |
| Linear search | `SearchEngine.linearSearchByTitle/Author()` |
| Binary search | `SearchEngine.binarySearchByTitle()` (when sorted by title) |
| Recursive search | `SearchEngine.recursiveSearchByTitle/Author()` |
| Selection/Insertion/Merge/Quick Sort | `SortAlgorithms` with GUI dropdown |
| Recursion | Recursive search, category count, overdue charge computation |
| GUI tabs | View Items, Borrow/Return, Admin, Search & Sort, Reports |
| Advanced GUI | Custom table renderer, dynamic form fields, file chooser, timers, validation, tooltips, mnemonics |
| Persistence | JSON save/load via `FileHandler` |
| Reports | Most borrowed, overdue users, category distribution |

## How to Run

```bash
# Launch the GUI
./run.sh

# Run unit tests (39 tests)
./run-tests.sh
```

Manual compile/run:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-23.jdk/Contents/Home
mkdir -p out && javac -d out $(find src -name "*.java")
java -cp out gui.MainWindow
```

Requires Java 17+ (uses switch expressions and pattern matching).

## Tests

The `tests/` folder contains a lightweight test suite (no JUnit dependency):

| Test class | What it covers |
|---|---|
| `SearchEngineTest` | Linear, binary, recursive search; search by type |
| `SortAlgorithmsTest` | Selection, insertion, merge, quick sort |
| `BorrowControllerTest` | Borrow, return, waitlist queue, auto-assign |
| `ItemCacheTest` | Fixed-size array cache access tracking |
| `LibraryManagerTest` | Undo stack, recursive charge/count, polymorphism |

## Usage Guide

1. **View Items** — Browse the catalogue; green = available, yellow = borrowed, red = overdue
2. **Borrow/Return** — Select user and item; use Reserve for waitlist when item is unavailable
3. **Admin** — Add items (dynamic fields per type) or users; Undo reverses last admin action
4. **Search & Sort** — Pick search algorithm and sort field/algorithm from dropdowns
5. **Reports** — Generate reports; Save/Load data to a folder containing `items.json` and `users.json`

## Submission Checklist

- [ ] Source code folder (`src/`)
- [ ] GUI screenshots (run app and capture each tab)
- [ ] UML class hierarchy diagram
- [ ] 2–3 page report (description, features, data structures, algorithms, challenges)

## Sample UML Class Hierarchy

```
<<interface>>          <<abstract>>
  Borrowable              LibraryItem
       △                        △
       │                        │
       └──────── implements ────┤
                                ├── Book
                                ├── Magazine
                                └── Journal

LibraryDatabase ──contains──▶ ArrayList<LibraryItem>
                            ArrayList<UserAccount>
                            Queue<String> (reservations)
                            Stack<AdminAction> (undo)

LibraryManager ──uses──▶ SearchEngine, BorrowController, ItemCache
```
# miva-java-project
