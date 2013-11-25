package at.jku.se.eatemup.core.logging;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "log")
public class LogEntry {
	@DatabaseField(id = true)
	public String id;
	@DatabaseField(index = true)
	public Date created;
	@DatabaseField
	public String text;
}
