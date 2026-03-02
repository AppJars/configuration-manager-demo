# Configuration Manager Demo

A runnable demo of the [Configuration Manager](https://docs.appjars.com/configuration-manager/overview/) AppJar: store, version and resolve application configuration from a database (system-wide or per user), and read it straight into your Spring beans through `@Value`.

The demo runs in **free mode**: every feature is fully functional, limited to 5 configurations you create (the preloaded examples don't count toward the limit). A full license removes the limit and changes nothing else.

## Running the demo

The project is a standard Maven project whose default goal is `spring-boot:run`. Run `mvn` from the project root, then open http://localhost:8080 in your browser.

The application starts on a public landing page that presents the AppJar features, the demo roles (`USER` and `ADMIN`, picked on the login screen, no password needed), the free/full license model, and guided tours of each view. A **Guided tour** menu is also available in the navigation bar, so a tour can be started from any view.

You can also import the project to your IDE of choice as you would with any Maven project. Read more on [how to import Vaadin projects to different IDEs](https://vaadin.com/docs/latest/flow/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

The AppJar artifacts are published on `https://maven.appjars.com`, which the `pom.xml` declares, so no extra Maven setup is needed.

## What you can explore

| View | Role | What it shows |
|---|---|---|
| My Configurations | `USER`, `ADMIN` | Self-service view of your own configuration values, with history and restore. |
| Current Configurations | `ADMIN` | How stored values reach the code through `@Value`, and how bean scope decides when a change takes effect (live vs. frozen). |
| System Configurations | `ADMIN` | Full CRUD over the configurations that apply to the whole application, plus a **Reload configuration** action that restarts the Spring context. |
| User Configurations | `ADMIN` | The same CRUD over per-user configurations, for any user. |

Each row of the configuration grids has a **⋮** menu with **Edit value**, **Show history**, **Restore value**, **Edit configuration** and **Delete configuration**; **New configuration** in the toolbar creates one.

Sample configurations of every supported type (String, Integer, Date, Time, Boolean, BigDecimal) are seeded on first startup, so every view has something to show. Their names start with `com.appjars.`, which the AppJar excludes from the free-tier count, so the 5 creations of free mode are all yours.

## Configuration

Every property below is listed in `src/main/resources/application.properties`, commented out where the demo uses the AppJar default. Note that an empty value overrides the default rather than falling back to it, so a property you do not want to change stays commented out.

### View routes

| Property | What it does | Default |
|---|---|---|
| `com.appjars.configurationmanager.url.views.systemconfigurationsview` | Route of the System Configurations view | `cfg/system` |
| `com.appjars.configurationmanager.url.views.usersconfigurationsview` | Route of the User Configurations view | `cfg/user/list` |
| `com.appjars.configurationmanager.url.views.myconfigurationsview` | Route of the My Configurations view | `cfg/user/my-configs` |

### Date and time formats

| Property | What it does | Default |
|---|---|---|
| `com.appjars.configurationmanager.dateformat` | Date pattern of the filter date pickers in the configuration list views | `dd-MM-yy` |
| `com.appjars.configurationmanager.datetimeformat` | Date and time pattern of the **Created** column in the configuration list views | `dd-MM-yyyy HH:mm:ss` |

### Application reload

| Property | What it does | Default |
|---|---|---|
| `com.appjars.configurationmanager.restart.enabled` | Shows the **Reload configuration** button in the System Configurations view. The demo sets it to `true`. | `false` |
| `com.appjars.configurationmanager.restart.reloadDelayMillis` | Delay before the browser reloads after a restart, in milliseconds | `5000` |

### Demo database

The demo stores its data in a file-based H2 database under `./data`, created on first startup. Delete that directory to start from scratch, with the sample configurations seeded again.

The **H2 web console is enabled** (`spring.h2.console.enabled=true`) at http://localhost:8080/h2-console, so you can inspect the tables the AppJar creates. Connect with the JDBC URL `jdbc:h2:./data/configurationmanagerdb`, user `sa` and no password. It is on for convenience in a throwaway demo; do not enable it in an application of your own.

## Deploying to production

To create a production build, run `mvn clean package -Pproduction`. This builds a JAR with all the dependencies and front-end resources, ready to be deployed, into the `target` folder.

Once the JAR file is built, you can run it with `java -jar target/appjars-configuration-manager-demo-2.0.0.jar`

## Project structure

- `HomeView.java` is the public landing page mounted on the root route.
- `MainLayout.java` contains the navigation setup (drawer, view title and the guided-tour menu), built on [App Layout](https://vaadin.com/components/vaadin-app-layout).
- `LoginDemoView.java` is the demo's role picker; it stands in for the real authentication of a final application.
- `CurrentConfigurationsView.java` is demo-specific: it reads the stored configurations through `@Value` to illustrate live vs. frozen resolution.
- The `views.tour` package builds the guided tours, on the Driver.js engine of the [antler-tour](https://vaadin.com/directory/component/antler-tour) add-on.
- `DemoService.java` seeds the sample configurations and provides the logged-in username to the AppJar.
- `src/main/resources/messages_en.properties` holds every user-visible string.
- `src/main/resources/META-INF/resources/styles.css` holds all the custom CSS.

The configuration views themselves come from the AppJar and are not part of this repository.

## Useful links

- [Configuration Manager documentation](https://docs.appjars.com/configuration-manager/overview/)
- [AppJars](https://www.appjars.com): get a license
- [AppJars on GitHub](https://github.com/AppJars)
- [Vaadin documentation](https://vaadin.com/docs)
