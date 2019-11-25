operation = { ->
	inside "build/tmp/nested.tar", {
		inside "./build/tmp/a.zip", {
			{ ->
				execute("touch", "newfile")
			}()
		}
	}
}
