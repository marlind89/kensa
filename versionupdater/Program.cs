// See https://aka.ms/new-console-template for more information
using System.Diagnostics;
using System.Text.Json;
using System.Text.RegularExpressions;
using System.Xml.Linq;

XNamespace ns = "http://maven.apache.org/POM/4.0.0";
const string currentVersionsFile = "currentVersions.json";

var versionRanges = XElement.Load("pom.xml")
    .Descendants(ns + "dependency")
    .Where(x => Regexes.VersionRangeRegex().IsMatch((string) x.Element(ns + "version")!))
    .Select(x => new Dependency(
        (string) x.Element(ns + "groupId")!,
        (string) x.Element(ns + "artifactId")!,
        (string) x.Element(ns + "version")!))
    .ToList();

HashSet<Dependency> previousVersions;
using (var stream = File.OpenRead(currentVersionsFile))
{
    previousVersions = File.Exists(currentVersionsFile)
        ? JsonSerializer.Deserialize<HashSet<Dependency>>(stream) ?? []
        : [];
}
    

var mvnDependencyProcess = Process.Start(new ProcessStartInfo()
{
    FileName = "cmd.exe",
    ArgumentList = {"/c", "mvn", "dependency:list" },
    RedirectStandardOutput = true,
    UseShellExecute = false,
    CreateNoWindow = true
});

ArgumentNullException.ThrowIfNull(mvnDependencyProcess);

var currentVersions = mvnDependencyProcess.StandardOutput.ReadToEnd()
    .Split(Environment.NewLine)
    .Select(x => Regexes.DependencyRegex().Match(x))
    .Where(x => x.Success)
    .Select(x => new Dependency(x.Groups[1].Value, x.Groups[2].Value, x.Groups[3].Value))
    .Where(x => versionRanges.Any(p => p.GroupId == x.GroupId && p.ArtifactId == x.ArtifactId))
    .ToHashSet();

if (previousVersions.SetEquals(currentVersions))
{
    Console.WriteLine("No changes in dependencies");
    return;
}

File.WriteAllText(currentVersionsFile, JsonSerializer.Serialize(currentVersions,
    new JsonSerializerOptions { WriteIndented = true }));

Console.WriteLine("Changes in dependencies. Start deploy.");

Process.Start(new ProcessStartInfo()
{
    FileName = "cmd.exe",
    ArgumentList = { "/c", "deploy.sh" },
    UseShellExecute = false,
    CreateNoWindow = true
})?.WaitForExit();

_ = 0;

record Dependency(string GroupId, string ArtifactId, string Version);

partial class Regexes
{
    [GeneratedRegex(@"\[\d+\.\d+\.\d+,\)")]
    public static partial Regex VersionRangeRegex();

    //com.github.devoxin:lavaplayer-natives-fork:jar:2.0.0:runtime
    [GeneratedRegex(@"\[INFO\]\s+(.+):(.+):.+:(\d+\.\d+\.\d+).*")]
    public static partial Regex DependencyRegex();
}