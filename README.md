## Abstract

_Bkell_ provides a Shell and API for maintaining balanced records for business transactions. It employs double-entry bookkeeping, where every entry to an account requires a corresponding and opposite entry to a different account. For instance, recording earnings of $100 would require making two entries: a debit entry of $100 to an account called "Cash" and a credit entry to an account called "Income."

_Bkell_ uses the American Equation approach to balancing books. With it, transactions are recorded based on the equation `Assets = Liabilities + Capital`. The accounting equation is a statement of equality between the debits and the credits. The rules of debit and credit depend on the nature of an account. For the purpose of the accounting equation approach, all the accounts are classified into the following five types:
- assets
- liabilities 
- income/revenues 
- expenses
- capital (gains / losses)

If there is an increase or decrease in one account, there will be equal decrease or increase in a corresponding account or set of accounts. The rules are summarised below:

           | debit | credit
-----------|-------|-------
asset      |   +   |   -
liability  |   -   |   +
revenue    |   -   |   +
expense    |   +   |   -
capital    |   -   |   +

For a more detailed description of double-entry bookkeeping concepts, refer to [Wikipedia](http://en.wikipedia.org/wiki/Double-entry_bookkeeping_system).

## Running

Starting a repl should drop you into the shell namespace `bkell.bkell`. You can start and stop the system with the `(start)` `(stop)` commands. Or you can simply get a list of commands by executing `help`.
```
=> (start)
#sys{:spittoon #sp{...} :bkell #bk{...}}

=> (stop)
#sys{:spittoon #sp{...} :bkell #bk{}}

=> (help)
```

#### Tests

You can run the project's tests with this command
```
$ lein midje
```

## Architecture 

The user works in a shell environment, manipulating the core data structures of the system. Bkell uses [Clojure](http://clojure.org/) for its runtime, and [Datomic](http://www.datomic.com/) as its datastore. The system is comprised, mostly of simple functions, and a data abstraction layer to push and pull data, into and out of the database. 

One of the benefits of Datomic, is that the storage engine can live in your runtime, on your local machine, or on a remote server. As such, Bkell can be embedded in a larger system's runtime, or as a RESTful service. The client system simply calls Bkell's API functions to manipulate the core data structures. Additionally, you can attach any user interface on top of it. 

#### Data Structure

These are the core entities in the system. The main business transactions are adding and manipulating entries, against a given set of accounts. All those business operations happen within a Journal, of a Book, for a given Group, which has Users attached. 

  - **Group**
    - can only have 1 owner 
    - can have many users
    - can have many books 
    - can't delete a group. instead delete its owner

  - **User**
    - belongs to at least 1 group 
    - can belong to many groups 
    - must own at least one group 
    - cannot update fields of another user
    - deleting a user also deletes the owned group (& bookkeeping data)
    
  - **Book**
    - 1 book can have many accounts 
    - 1 book should have only 1 journal 
    - only a group's users (incl. owner) can CRUD this data

  - **Journal**
    - each journal can have many entries 
    - only a group's users (incl. owner) can CRUD this data
    
  - **Account**
    - account addition or updates do not bleed into another group's accounts
    - no duplicates
    - only a group's users (incl. owner) can CRUD this data
    - can't delete an account with dependent entries

  - **Entry**
    - each entry must be balanced 
    - query within date ranges
    - only a group's users (incl. owner) can CRUD this data

#### Data Access

This is a matrix of the data creation strategies for each given environment.

Env / DB   | create |conn |init |import
-----------|--------|-----|-----|-------
at repl    |        |  Y  |     |  Y
with tests |   Y    |  Y  |  Y  |
in PROD    |        |  Y  |     |
import 1   |   Y    |  Y  |  Y  |  Y
improt 2   |        |  Y  |     |  Y



## Todo List

- separate import data function (we need insert-in, then we can constrain the data being inserted)
- import default data on start (:test)
  - import users (under :users), if you are the group owner
  - import accounts, journals, and journal entries (under :books), if your user is a member of the group
- separate export data function
- change password on user creation
- add-account helper that assigns :counterWeight, based on the :type

- use Maybe Monad to execute component.spittoon/start
- datomic wrapper (using adi)
  - with nominal CRUD operations
  - with {create,retrieve,update,delete}-in, constraining the data's context
  - the abouve data structure constraints must be maintained when manipulating entites in the system.
  - each CRUD operation should be programmed with a corresponding test.check function ; either create, or reuse the correct generators for the task 
- login mechanism for the shell; (ref: crash pluggable authentication: http://www.crashub.org/1.2/reference.html#pluggable_auth)
- runnable scripts ; include code examples for adding account(s) and entries 
- don't see a way to disconnect from a datomic DB (worried about lingering connection issues)
- don't think we need container pointers in schema
```
:entry {:content [{:type :ref
                   :ref {:ns :side}
                   :cardinality :many
                   :isComponent true}]
        :journal [{:type :ref
                   :ref {:ns :journal}
                   :doc "The journal belonging to the entry"}]}
```
- put data contraints in as Datomic Transaction Queries (http://docs.datomic.com/database-functions.html)
  - don't add duplicate accounts (solely within a given group)

- for function constraints, evaluate typed.clojure vs. schema
- an aggreagate function : sum of all credit accounts must equal sum of all debit accounts
- replace `no-duplicate-accounts` with a query using the list
- CRUD on book(s) journal(s)
- make better generators for functions in domain/{account.clj,entry.clj}
- ensure that :account-counterweight is in the input structure of entry-balanced?
- add IDs to account and entry; we need to ensure that there's a way to differentiate, say, entries with the same data
- reverse an entry 
- remaining updates
  - group
    - name
    - owner
    - defaultCurrency
    + add a user
    + add a set of books
  - user
    - username
    - password
    - firstname
    - lastname
    - email
    - country
    [x] not defaultGroup
- remaining deletes
  - account 
  - entry 
  - group 
  - user

- (entry) test unbalanced
- (entry) test non-existant accounts
- (entry) a bigger entry example
- (entry) test with "find-corresponding-account-byid"
- (entry) test that entry gets into the correct group

- on add-account, assert that :type has the correct corresponding :counterWeight
-- currently only operating on the `main` books; open this up to operate on any book
- ** howto assert that account belongs to group (in: domain.account/find-account-by-id)

- ** There's a schema problem, when trying to update an entry, directly by id 

## License

Copyright © 2014 Interrupt Software Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
