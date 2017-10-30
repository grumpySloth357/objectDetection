all: a4

objects = $(patsubst %.cpp, %.o, $(wildcard ./*.cpp))

%.o: %.cpp
	g++ -c -g -O2 `pkg-config --cflags opencv` -o $@ $< 

a4: $(objects)
	g++ -g -O2 `pkg-config --libs opencv` -o $@ $^

run: a4
	./a4

.phony: clean

clean:
	rm *.o a4
