import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher

class SawfilesContext {
	File workspace

	SawfilesContext(File workspace) {
		this.workspace = workspace
	}

	def static fileTypes = [
		".tar": [{file -> ["tar", "xf", file]}, {file -> ["tar", "cf", file, "."]}],
		".zip": [{file -> ["unzip", file]}, {file -> ["zip", file, "-r", "."]}]
	]

	void operate(String path, String extension, Closure closure) {
		def abs = new File(workspace, path).getAbsoluteFile()
		def w = Files.createTempDirectory("sawfiles-").toFile()
		new ProcessBuilder(fileTypes[extension][0](abs.getPath())).directory(w).start().waitFor()
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = new SawfilesContext(w)
		closure()
		abs.delete()
		new ProcessBuilder(fileTypes[extension][1](abs.getPath())).directory(w).start().waitFor()
	}

	/*
	 * a work-around for following two methods
	 *  void inside(String path, Closure closure)
	 *  void inside(Map files, Closure c)
	 */
	Closure getInside() {
		return {target, closure ->
			if (target in Map) {
				target.each { path, extension ->
					operate(path, extension, closure)
				}
			} else {
				operate(target, target.substring(target.lastIndexOf(".")), closure)
			}
		}
	}

	/*
	 * a workaround for following method
	 *  SawfilesFilter glob(String pattern)
	 */
	Closure getGlob() {
		return {String pattern ->
			def matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern)
			return new SawfilesFilter({ path -> matcher.matches(path) });
		}
	}

	/*
	 * a workaround for following method
	 *  SawfilesFilter regex(String pattern)
	 */
	Closure getRegex() {
		return {String pattern ->
			def matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern)
			return new SawfilesFilter({ path -> matcher.matches(path) });
		}
	}

	/*
	 * a workaround for following method
	 *  List list(SawfilesFilter filter)
	 */
	Closure getList() {
		return {SawfilesFilter filter ->
			List found = []
			filter.list(workspace, workspace, found)
			return found
		}
	}

	/*
	 * a work-around for following method
	 *  void execute(String... args)
	 */
	Closure getExecute() {
		return {String... args -> new ProcessBuilder(args).directory(workspace).start().waitFor()}
	}

	String toString() {
		return "SawfilesContext(" + workspace + ")"
	}
}

class SawfilesFilter {
	Closure closure

	SawfilesFilter(Closure closure) {
		this.closure = closure
	}

	boolean matches(Path path) {
		return closure(path)
	}

	SawfilesFilter and(SawfilesFilter another) {
		return new SawfilesFilter({ path -> this.matches(path) && another.matches(path) })
	}

	SawfilesFilter or(SawfilesFilter another) {
		return new SawfilesFilter({ path -> this.matches(path) || another.matches(path) })
	}

	SawfilesFilter bitwiseNegate() {
		return new SawfilesFilter({ path -> ! this.matches(path) })
	}

	void list(File root, File base, List found) {
		base.listFiles().each { file ->
			Path path = root.toPath().relativize(file.toPath())
			if (matches(path)) {
				found.add(path.toString())
			}
			if (file.isDirectory()) {
				list(root, file, found)
			}
		}
	}
}

GroovyShell shell = new GroovyShell()
Script script = shell.parse(new java.io.FileReader(args[0]))
script.args = args[1..<args.length]
script.run()
script.operation.delegate = new SawfilesContext(new File(System.getProperty("user.dir")))
script.operation()
