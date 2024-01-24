SRC_DIR := src
OUT_DIR := bin

SRCS := $(wildcard $(SRC_DIR)/*/*.java)
CLS := $(SRCS:$(SRC_DIR)/%.java=$(OUT_DIR)/%.class)

JC := javac
JCFLAGS := -encoding iso-8859-1 -Xlint:all -d $(OUT_DIR)/ -cp $(SRC_DIR)/

.SUFFIXES: .java .class

.PHONY: all project clean

all: done

done: $(SRCS)
	$(JC) $(JCFLAGS) $?
	touch done

clean:
	rm -rf $(OUT_DIR) done
