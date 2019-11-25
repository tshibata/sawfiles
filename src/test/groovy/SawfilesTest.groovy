import spock.lang.Specification
import java.nio.file.Files

class SawfilesTest extends Specification {
	def "modify a tar file"() {
		setup:
		["rm", "-f", "build/tmp/a.tar"].execute().waitFor()
		["tar", "cf", "build/tmp/a.tar", "src/test/resources/hello.txt"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/tar.dsl")

		then:
		["tar", "tf", "build/tmp/a.tar"].execute().text.contains("newfile")
	}
	def "modify a zip file"() {
		setup:
		["rm", "-f", "build/tmp/a.zip"].execute().waitFor()
		["zip", "build/tmp/a.zip", "src/test/resources/hello.txt"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/zip.dsl")

		then:
		["unzip", "-l", "build/tmp/a.zip"].execute().text.contains("newfile")
	}
	def "modify a zip in a tar"() {
		setup:
		["rm", "-f", "build/tmp/a.zip"].execute().waitFor()
		["zip", "build/tmp/a.zip", "src/test/resources/hello.txt"].execute().waitFor()
		["rm", "-f", "build/tmp/nested.tar"].execute().waitFor()
		["tar", "cf", "build/tmp/nested.tar", "build/tmp/a.zip"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/nested.dsl")

		["tar", "xfC", "build/tmp/nested.tar", "build/tmp"].execute().waitFor()

		then:
		["unzip", "-l", "build/tmp/build/tmp/a.zip"].execute().text.contains("newfile")
	}
	def "modify a zip in a tar with one more closure"() {
		setup:
		["rm", "-f", "build/tmp/a.zip"].execute().waitFor()
		["zip", "build/tmp/a.zip", "src/test/resources/hello.txt"].execute().waitFor()
		["rm", "-f", "build/tmp/nested.tar"].execute().waitFor()
		["tar", "cf", "build/tmp/nested.tar", "build/tmp/a.zip"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/nested2.dsl")

		["tar", "xfC", "build/tmp/nested.tar", "build/tmp"].execute().waitFor()

		then:
		["unzip", "-l", "build/tmp/build/tmp/a.zip"].execute().text.contains("newfile")
	}
	def "copy *.txt"() {
		setup:
		["rm", "-fr", "build/tmp/text"].execute().waitFor()
		["mkdir", "build/tmp/text"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/glob.dsl")

		then:
		0 < ["ls", "build/tmp/text"].execute().text.length()
	}
	def "mixture of features"() {
		setup:
		["rm", "-f", "build/tmp/build/tmp/a.zip"].execute().waitFor()
		["rm", "-f", "build/tmp/a.zip"].execute().waitFor()
		["zip", "build/tmp/a.zip", "src/test/resources/tmp/bye.txt"].execute().waitFor()
		["rm", "-f", "build/tmp/zips.tar"].execute().waitFor()
		["tar", "cf", "build/tmp/zips.tar", "build/tmp/a.zip"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/mix.dsl")

		["tar", "xfC", "build/tmp/zips.tar", "build/tmp"].execute().waitFor()

		then:
		! (["unzip", "-l", "build/tmp/build/tmp/a.zip"].execute().text.contains("tmp/\n"))
	}
}
