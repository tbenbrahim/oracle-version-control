All commands must be run in the working directory used during the `init` command.

__Configuration__

`ovcs config key value`

Configure ovcs for first time use. The following settings are supported:

- oracle-driver sets the location of the Oracle JDBC library

_Example:_ (actual path may vary)

on Windows: `ovcs config oracle-driver c:\oracle\app\oracle\product\11.2.0\server\jdbc\lib\ojdbc6.jar`

or

on Linux/OS X : `ovcs config oracle-driver /usr/local/oracle/server/jdbc/lib/ojdbc6.jar`


__Schema initialization__

`ovcs init oracle-connection-string git-repository-uri`
	
Initialize the current directory as the local file store for schema objects, with the specified Oracle connection string and the URI of the GIT repository that will hold the schema objects for the connection string's schema. The specified directory must be empty. This command is run once by a developer for each schema of interest, using a new directory for each schema.

_Example:_

`ovcs init HR/HR@localhost:1521:XE https://jsmith:password@github.com/jsmith/hr-schema.git`
    
__Session start__

`ovcs start`

Starts an editing session. 

__Status__

`ovcs status`

View pending changes. 

__Diff__

`ovcs diff object-name`

Show differences in an object.

_Example:_

`ovcs diff COUNTRIES`

	
__Commit changes__

`ovcs commit`

Ends an editing session, and commits changed objects to the repository. 

__Push Changes__

`ovcs push`

Push all pending commits to the remote Git repository. 

