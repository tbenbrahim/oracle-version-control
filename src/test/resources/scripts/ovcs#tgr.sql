create or replace trigger ovcs#tgr before ddl on hr.schema
  declare
    v_source clob;
    v_type user_objects.object_type%type;
  begin
	if ora_dict_obj_name not like 'OVCS#%'then
	    begin
	      select object_type,
	        dbms_metadata.get_ddl(object_type, object_name)
	      into v_type,
	        v_source
	      from user_objects
	      where user_objects.object_name = ora_dict_obj_name and object_type not like '% BODY';
	
	    exception
	
	    when no_data_found then
	      v_source := null;
	
	    end;
	    ovcs.handler.process(ora_sysevent, ora_dict_obj_owner, ora_dict_obj_name, v_type, v_source) ;
	end if;
  end ovcs#tgr;