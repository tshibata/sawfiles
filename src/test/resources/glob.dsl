operation = { ->
	list(glob("src/**/*.txt")) each {txt ->
		execute("cp", txt, "build/tmp/text")
	}
}
