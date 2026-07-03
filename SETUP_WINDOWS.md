# Windows Setup - From Scratch (Java 25, Maven 3.9.16, Docker, Git SSH)

Everything here targets your machine: IntelliJ IDEA Ultimate, Docker Desktop 29.5.3,
Git 2.55.0, PowerShell (the terminal inside IntelliJ is fine for all of it), Postman.

Run every command in **PowerShell** (Windows Terminal or the IntelliJ terminal).
Open a **new** terminal after any step that changes environment variables.

| Tool         | Version we install         | Why                                         |
|--------------|----------------------------|---------------------------------------------|
| JDK          | Eclipse Temurin 25 (LTS)   | Boot 4.1 runs on Java 17-26; 25 is the LTS  |
| Maven        | Apache Maven 3.9.16        | Maven 4 is still RC; 3.9.16 is stable        |
| Spring Boot  | 4.1.0 (pinned in the pom)  | Current stable, Spring Framework 7           |
| Postgres     | 17 (Docker)                | Via docker compose - nothing to install      |

---

## 1. Install JDK 25 (Temurin) - answers "download JDK 25"

1. Go to the Adoptium download page:
   `https://adoptium.net/temurin/releases/?version=25&os=windows&arch=x64&package=jdk`
2. Pick **JDK 25 (LTS) / Windows / x64 / .msi** and download it (latest patch, e.g. `25.0.3`).
3. Run the MSI. On the **Custom Setup** screen, click each of these and choose
   "Will be installed on local hard drive":
   - **Add to PATH**
   - **Set or override JAVA_HOME variable**
   - (optional) Associate .jar files

   Those two checkboxes do steps 2 (PATH) and JAVA_HOME for you. Default install path:
   `C:\Program Files\Eclipse Adoptium\jdk-25.x.x-hotspot`.

4. Open a **new** PowerShell and verify:
   ```powershell
   java -version
   javac -version
   $env:JAVA_HOME
   ```
   You should see `openjdk version "25..."` and JAVA_HOME pointing at the jdk-25 folder.

---

## 2. JAVA_HOME + PATH (only if you skipped the MSI checkboxes)

If `java -version` or `$env:JAVA_HOME` is wrong, set them manually (User-level, no admin needed):

```powershell
# Point JAVA_HOME at your JDK 25 (adjust the folder name to what you installed)
[Environment]::SetEnvironmentVariable(
  'JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot', 'User')

# Add %JAVA_HOME%\bin to the front of your User PATH
$p = [Environment]::GetEnvironmentVariable('Path','User')
[Environment]::SetEnvironmentVariable('Path', "$env:JAVA_HOME\bin;$p", 'User')
```

Close and reopen PowerShell, then re-run the verify block from step 1.
(GUI alternative: Start > "Edit the system environment variables" > Environment Variables.)

---

## 3. Install Maven 3.9.16 - answers "download Maven, validate in command line"

Maven needs no installer - it is a zip you extract and put on PATH.

1. Download `apache-maven-3.9.16-bin.zip` from `https://maven.apache.org/download.cgi`
   (under **Files**, the "Binary zip archive").
2. Extract it to a tools folder, e.g. `C:\tools\apache-maven-3.9.16`
   (avoid spaces in the path).
3. Set `MAVEN_HOME` and add its `bin` to PATH:
   ```powershell
   [Environment]::SetEnvironmentVariable(
     'MAVEN_HOME', 'C:\tools\apache-maven-3.9.16', 'User')
   $p = [Environment]::GetEnvironmentVariable('Path','User')
   [Environment]::SetEnvironmentVariable('Path', "$env:MAVEN_HOME\bin;$p", 'User')
   ```
4. Open a **new** PowerShell and validate:
   ```powershell
   mvn -v
   ```
   Expect `Apache Maven 3.9.16`, and a `Java version: 25...` line (Maven uses JAVA_HOME).
   If you see a one-line warning like "A restricted method in java.lang.System has been
   called" on Java 25, it is harmless.

---

## 4. Local Maven repository (the `.m2` repo) - answers "set up m2_repo"

Maven caches every downloaded jar in a local repository. Default location:
`C:\Users\<you>\.m2\repository`. That default is fine - you do not have to change it.

If you want to control it explicitly (recommended so IntelliJ and the CLI agree), create
a `settings.xml`:

```powershell
New-Item -ItemType Directory -Force "$env:USERPROFILE\.m2" | Out-Null
@"
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <localRepository>${env:USERPROFILE}\.m2\repository</localRepository>
</settings>
"@ | Set-Content -Encoding UTF8 "$env:USERPROFILE\.m2\settings.xml"
```

Verify Maven resolves that path:
```powershell
mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

> First build will download Boot 4.1 + Spring 7 + springdoc into this repo. Requires
> internet the first time; afterwards it is cached.

---

## 5. IntelliJ - point the project at JDK 25 - answers "right JDK/JRE in IntelliJ"

1. **Register the SDK** (once): `File > Project Structure > Platform Settings > SDKs > +
   > Add JDK` and select `C:\Program Files\Eclipse Adoptium\jdk-25...`. It appears as
   `temurin-25` (or `25`).
2. **Project SDK + language level**: `File > Project Structure > Project`
   - **SDK**: `temurin-25`
   - **Language level**: `25`
3. **Maven importer JDK**: `File > Settings > Build, Execution, Deployment > Build Tools
   > Maven > Importing` (or the "JDK for importer" dropdown) and the **Runner** page -
   set both to `temurin-25`. This makes IntelliJ compile and run with the same JDK the CLI uses.
4. Because compile level is driven by the pom (`<java.version>25</java.version>`),
   you do not need to set bytecode version by hand - IntelliJ reads it on import.

---

## 6. IntelliJ - point at your Maven + repo - answers "set up Maven and m2 in IntelliJ"

`File > Settings > Build, Execution, Deployment > Build Tools > Maven`:
- **Maven home path**: `C:\tools\apache-maven-3.9.16` (or leave "Bundled (Maven 3)" -
  IntelliJ's bundled Maven also works; using your own keeps CLI and IDE identical).
- **User settings file**: check "Override" and point to `C:\Users\<you>\.m2\settings.xml`.
- **Local repository**: should auto-fill to `...\.m2\repository`. If not, set it.

Then open the project: `File > Open` and select the **top-level `pom.xml`** (the one in
`arch-insurance-lab\`). Choose "Open as Project". IntelliJ imports all three modules.
Click the Maven tool window's refresh (circular arrows) to download dependencies.

---

## 7. IntelliJ - Spring setup / plugins - answers "any specific plugin"

**Ultimate already bundles everything you need** - do not install third-party Spring plugins.
Confirm these are enabled under `File > Settings > Plugins > Installed` (they are on by default):
- **Spring Boot**, **Spring**, **Spring Data**, **Spring Web** - give you the run
  dashboard, `application.yml` completion, bean/endpoint navigation, and config hints.
- **Database Tools and SQL** - the built-in DB client (used in the README to browse Postgres).
- **Docker** - manage the Postgres container from inside the IDE.

> The third-party "Spring Assistant" plugin is for **Community** edition. On Ultimate it is
> redundant - skip it.

After import, open the **Services** tool window (`Alt+8` / `View > Tool Windows > Services`).
Your three Spring Boot apps appear there as a run dashboard once you create run configs
(just click the green arrow on each `*Application.java`).

---

## 8. Postgres via Docker - answers "download Postgres from Docker Hub"

**You do not need to `docker pull` anything or log into Docker Hub for this project.**
`docker compose up` reads `docker-compose.yml`, pulls the public `postgres:17` image
anonymously, creates the three databases, and starts the container. That is the whole flow:

```powershell
cd path\to\arch-insurance-lab
docker compose up -d           # first run pulls postgres:17, then starts it
docker compose ps              # STATUS should show "healthy"
docker compose logs -f postgres   # optional: watch init (Ctrl+C to stop tailing)
```

Stop / reset:
```powershell
docker compose down            # stop, keep data volume
docker compose down -v         # stop AND delete data (fresh databases next up)
```

### If you specifically want to pull an image yourself (optional)
Browsing/selecting any Postgres version is just a tag on the public image:
```powershell
docker pull postgres:17            # what this project uses
docker pull postgres:16            # a different major version
docker pull postgres:17-alpine     # smaller image
docker images postgres             # list what you have locally
```
Available tags are listed at `https://hub.docker.com/_/postgres`. To use a different
version in this project, change `image: postgres:17` in `docker-compose.yml` and re-run
`docker compose up -d` (run `docker compose down -v` first if the major version changes).

### When you actually need a Docker Hub account (optional)
Only if you hit the anonymous pull rate limit, or want private images. Then:
```powershell
docker login                       # enter your Docker Hub username + a Personal Access Token
```
Create the token at Docker Hub > Account Settings > Personal access tokens. For this
lab's public Postgres image, login is **not** required.

---

## 9. Git over SSH as "cloudeddeveloper" - answers "connect Git via SSH"

Uses Windows' built-in OpenSSH (works directly in PowerShell). "cloudeddeveloper" is your
GitHub username; the SSH key proves it is you.

**a. Create a key** (ed25519 - modern default):
```powershell
ssh-keygen -t ed25519 -C "cloudeddeveloper@github"
# Press Enter to accept the default path: C:\Users\<you>\.ssh\id_ed25519
# Set a passphrase (recommended) or Enter for none
```

**b. Start the ssh-agent and add the key:**
```powershell
# Enable the Windows OpenSSH agent (admin PowerShell for Set-Service; one-time)
Get-Service ssh-agent | Set-Service -StartupType Automatic
Start-Service ssh-agent
ssh-add "$env:USERPROFILE\.ssh\id_ed25519"
```

**c. Tell Git to use Windows OpenSSH** (so it shares this agent):
```powershell
git config --global core.sshCommand "C:/Windows/System32/OpenSSH/ssh.exe"
```

**d. Add the public key to GitHub:**
```powershell
Get-Content "$env:USERPROFILE\.ssh\id_ed25519.pub" | Set-Clipboard
```
GitHub > Settings > **SSH and GPG keys** > **New SSH key** > paste > Add.

**e. Test the connection:**
```powershell
ssh -T git@github.com
# Expect: "Hi cloudeddeveloper! You've successfully authenticated..."
```

**f. Set your commit identity** (separate from the SSH key - this is the author on commits):
```powershell
git config --global user.name  "Awnish Bhatt"
git config --global user.email "you@example.com"
```

**g. Create an empty repo on GitHub** named `arch-insurance-lab` (do NOT add a README/gitignore).

**h. Push this project:**
```powershell
cd path\to\arch-insurance-lab
git init
git add .
git commit -m "Initial commit: quote-to-bind microservices (Java 25, Spring Boot 4.1)"
git branch -M main
git remote add origin git@github.com:cloudeddeveloper/arch-insurance-lab.git
git push -u origin main
```

Subsequent pushes are just `git add -A; git commit -m "..."; git push`.

---

## Appendix - verify the whole toolchain

```powershell
java -version          # openjdk 25
javac -version         # javac 25
$env:JAVA_HOME         # ...\jdk-25...
mvn -v                 # Apache Maven 3.9.16, Java version 25
docker --version       # Docker 29.5.3
docker compose version # v2.x
git --version          # git 2.55.0
ssh -T git@github.com  # Hi cloudeddeveloper!
```

If all of these pass, jump to `README.md` to build and run.
