## Abstract

_Bkell_ provides a Shell and API for maintaining balanced records for business transactions. It employs double-entry bookkeeping, where every entry to an account requires a corresponding and opposite entry to a different account. For instance, recording earnings of $100 would require making two entries: a debit entry of $100 to an account called "Cash" and a credit entry to an account called "Income."

For a more detailed description of double-entry bookkeeping concepts, refer to [Wikipedia](http://en.wikipedia.org/wiki/Double-entry_bookkeeping_system).

## Architecture 

The user works in a shell environment, manipulating the core data structures of the system. Bkell uses [Clojure](http://clojure.org/) for its runtime, and [Datomic](http://www.datomic.com/) as its datastore. The system is comprised, mostly of simple functions, and a data abstraction layer to push and pull data, into and out of the database. 

One of the benefits of Datomic, is that the storage engine can live in your runtime, on your local machine, or on a remote server. As such, Bkell can be embedded in a larger system's runtime, or as a RESTful service. The client system simply calls Bkell's API functions to manipulate the core data structures. Additionally, you can attach any user interface on top of it. 

#### Data Structure

These are the core entities in the system. The main business transactions are adding and manipulating entries, against a given set of accounts. All those business operations happen within a Journal, of a Book, for a given Group, which has Users attached. 

  - **Group**
    - can only have 1 owner 
    - can have many users
    - can have many books 

  - **User**
    - belongs to at least 1 group 
    - can belong to many groups 
    - must own at least one group 
    - cannot update fields of another user

  - **Book**
    - 1 book can have many accounts 
    - 1 book should have only 1 journal 
    - only a group's users (incl. owner) can CRUD this data

  - **Journal**
    - each journal can have many entries 
    - only a group's users (incl. owner) can CRUD this data

  - **Account**
    - no duplicates
    - only a group's users (incl. owner) can CRUD this data

  - **Entry**
    - each entry must be balanced 
    - query within date ranges
    - only a group's users (incl. owner) can CRUD this data

## Todo List

- setup test infrastructure (using test.check)
- establish core data structure (datomic schema)
- datomic wrapper (using adi), with nominal CRUD operations; the abouve data structure constraints must be maintained when manipulating entites in the system.
  - each CRUD operation should be programmed with a corresponding test.check function ; either create, or reuse the correct generators for the task 
- import / export data 
- runnable scripts ; include code examples for adding account(s) and entries 


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.