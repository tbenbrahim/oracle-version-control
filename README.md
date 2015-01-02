__Configuration__

    ovcs config db ovcs/ovcs@localhost:1521:XE

__Schema initialization__
	
    ovcs init HR http://localhost/scm/hr.git
    
__Session start__

	ovcs start HR
	
__Session end__

	ovcs end HR
	Enter commit message, end with a single period at start of line to end
	modified countries table
	+review HR @john.doe
	.
