# Variables
JC = javac
JAVA = java
SRCDIR = .
BINDIR = bin
MAIN_CLASS = amatsagu.merito.Initializer
SOURCES = $(shell find $(SRCDIR) -name "*.java")

.DEFAULT_GOAL := run

compile: $(BINDIR)
	@echo "--- Compiling Java Sources ---"
	$(JC) -d $(BINDIR) $(SOURCES)
	@echo "--- Compilation successful. ---"

run: compile
	@echo "--- Starting Game ---"
	$(JAVA) -cp $(BINDIR) $(MAIN_CLASS)

$(BINDIR):
	mkdir -p $(BINDIR)

clean:
	@echo "--- Cleaning up compiled files ---"
	$(RM) -r $(BINDIR)

.PHONY: all compile run clean