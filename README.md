# Shadow LSP

A [Language Server Protocol](https://microsoft.github.io/language-server-protocol/) server for **Shadow**, a compiled language made by my professor, Dr. Barry Wittman. Built in Java with [LSP4J](https://github.com/eclipse-lsp4j/lsp4j) so you can actually get real-time compiler errors/warnings in your editor instead of running the compiler by hand every time.

Currently working on IDE integration for IntelliJ and VS Code.

## What it actually does

Basically, it hooks into Shadow's real compiler and runs it in the background whenever you open or save a file, then sends any errors/warnings straight to your editor so they show up as squiggly underlines like you'd expect from any normal language.

**What's working right now:**
- Tracks open/changed/closed/saved files
- Sends live diagnostics back to the editor
- Actually gets the error positions right (way harder than it sounds, more on that below)
- Uses Shadow's real `TypeChecker`, not some reimplementation

## How it's put together

- **`Compiler`** — wraps Shadow's actual `TypeChecker`, runs it on a file, and hands back a clean list of errors/warnings (no duplicates, more on that below too)
- **`DocumentService`** — the class that actually talks to the editor, handles file events, and turns compiler output into LSP diagnostics
- **`DocumentManager`** — just keeps track of what files are currently open and what they say
- **`LspServer`** — the main server class that ties everything together and talks to the editor over stdio

### Some things I had to figure out along the way

**Diagnostics only run when you save, not while you're typing.** Shadow's compiler reads files straight from disk and has zero support for checking unsaved content in memory. I could've tried writing unsaved buffers to temp files, but that opens up a whole can of worms with import resolution and project structure, plus it's kind of risky messing with the real file. So for now: save the file, get your diagnostics. Pretty standard for compiled languages anyway.

**The line/column math was NOT obvious and I had to test it by hand.** Turns out the compiler reports lines starting at 1 but columns starting at 0, and the end of an error range is inclusive — meanwhile LSP wants everything 0-indexed with an exclusive end. I didn't just assume this, I actually wrote a test file with a deliberate error, counted the exact position by hand in my editor, and compared it to what the compiler spit out. If I hadn't done this, my error squiggles would've been off by one in random, annoying ways.

**Found and fixed an actual bug in the compiler's error reporting.** For certain assignment statements, the compiler reports the exact same error twice. I traced it all the way down to the specific method in the type-checker causing it. Instead of touching the compiler itself, I just filter out duplicates before sending anything to the editor.

## Running it

**You need:** Java 17+, Maven

```bash
mvn test
```

The server runs over stdio like every other LSP server — point any LSP-compatible client at the built jar and it'll work.

## Testing

I wrote a JUnit suite that actually runs the real compiler (not mocks) to check:
- Clean files come back with zero errors
- Broken files come back with the right error
- The line/column conversion math is actually correct
- Results can't be accidentally mutated
- Opening/saving a file for real triggers a real compile

## What's left

Diagnostics are the main feature and they work. Still on the list:
- **Warnings** (unused fields/methods) — the compiler can technically find these, but it needs an extra compilation step I haven't wired in yet
- **Actual editor plugin** — the server itself doesn't care what editor you use, so I'm building IntelliJ/VS Code support next
- **Live checking while typing** — would need to solve the whole import-resolution-for-unsaved-files problem, so it's a "someday" thing, not ignored on purpose

## Why I built this

Wanted to actually learn how LSPs work instead of just using one, and get real experience debugging a real, non-trivial compiler instead of a toy project.
