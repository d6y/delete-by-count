# Possible delete regression in 3.1?

The following query...

```scala
// Delete all messages that have a duplicate (leaving zero rows with that content)
val zap =
  messages.filter { msg =>
    messages.filter(_.content === msg.content).size > 1
  }.delete
```

...produces different results with Slick 3.0 and 3.1

In a table containing the rows:

- Hello
- Hello
- Yo!

...I would expect the query to remove the first two rows.

- Under 3.0 it removes 2 rows (yay!)
- Under 3.1 it removes 3 rows (huh?).

This is with h2.

The queries are (3.0 then 3.1):

```sql
delete from "message" where (select count(1) from "message" x2 where x2."content" = "message"."content") > 1
delete from "message" where (select count(1) from "message" where "content" = "message"."content") > 1
```

Note the difference in the `x2` prefix.

## Steps to reproduce

1. Set the _build.sbt_ Slick version to 3.0 or 3.1
2. Exec `run` in sbt and observe the results below.

##  Results running with Slick 3.0

```
[info] Compiling 1 Scala source to /Users/richard/Developer/books/delete-by-count/target/scala-2.11/classes...
[info] Running Example
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: create table "message" ("content" VARCHAR NOT NULL)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "message" ("content")  values (?)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "message" ("content")  values (?)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "message" ("content")  values (?)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: delete from "message" where (select count(1) from "message" x2 where x2."content" = "message"."content") > 1
2
```


##  Results running with Slick 3.1

```
[info] Running Example
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: create table "message" ("content" VARCHAR NOT NULL)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "message" ("content")  values (?)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "message" ("content")  values (?)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: insert into "message" ("content")  values (?)
DEBUG slick.jdbc.JdbcBackend.statement - Preparing statement: delete from "message" where (select count(1) from "message" where "content" = "message"."content") > 1
3
```
