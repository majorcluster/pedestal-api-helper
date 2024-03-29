## params-helper

### Index
- Definitions
  - [validation-def](#validation-def)
  - [mop-fields-opts-def](#mop-fields-opts-def)
- Symbols
  - [uuid-pattern](#uuid-pattern)
- Functions
  - [extract-field-value](#extract-field-value)
  - [is-uuid](#is-uuid)
  - [mop-fields](#mop-fields)
  - [uuid](#uuid)
  - [uuid-as-string](#uuid-as-string)
  - [validate-and-mop!!](#validate-and-mop!!)

### Definitions
Patterns for some maps passed to the functions as arguments:
- <h3><a id='validation-def'></a><span style="color:coral">validation-def</span> ^map</h3><br>
| key              | value                  | description                                              | mandatory extra key      | optional extra key                                                                 |
| -----------------| -----------------------| -------------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------- |
| `:validate/type` | `:validate/mandatory`  | validates if field is present                            | -                        | `:validate/message` ^string with %s being field name                               |
| `:validate/type` | `:validate/max`        | validates if max is not reached                          | `:validate/value` ˆint   | `:validate/message` ^string with 1st %s being field name, 2nd being max value <br>`:validate/ignore-if-absent` ^boolean to ignore validation for absent field |
| `:validate/type` | `:validate/min`        | validates if at least min value is reached               | `:validate/value` ˆint   | `:validate/message` ^string with 1st %s being field name, 2nd being min value <br>`:validate/ignore-if-absent` ^boolean to ignore validation for absent field |
| `:validate/type` | `:validate/regex`      | validates if string matches regex pattern                | `:validate/value` ˆregex | `:validate/message` ^string with %s being field name <br>`:validate/ignore-if-absent` ^boolean to ignore validation for absent field                                |
| `:validate/type` | `:validate/custom`     | validates if custom fn receiving the value returns true  | `:validate/value` ^fn    | `:validate/message` ^string with %s being field name <br>`:validate/ignore-if-absent` ^boolean to ignore validation for absent field                                |
  - Examples:
  ```clojure
    {"field-name"
      [{:validate/type :validate/mandatory, :validate/message "%s is ..."},
       {:validate/type :validate/min, :validate/value 12, :validate/message "%s is mandatory"},
       {:validate/type :validate/max, :validate/value 40, :validate/message "%s is ..."},
       {:validate/type :validate/regex, :validate/value #"^[\d]{1,2}$", :validate/message "%s is ..."},
       {:validate/type :validate/custom, :validate/value fn, :validate/message "% is ..."}]}
  ```
- <h3><a id='mop-fields-opts-def'></a><span style="color:coral">mop-fields opts</span> ^map</h3><br>
| key              | value     / default    | description                                                          |
| ---------------- | ---------------------- | -------------------------------------------------------------------- |
| `:ignore-uuid`   | boolean   / false      | ignores string to uuid conversion, making the algorithm a bit faster |
  - Examples:
  ```clojure
    {:ignore-uuid true}
  ```

### Symbols
- <h3><a id='uuid-pattern'></a><span style="color:green">uuid-pattern</span><br></h3>
  UUID string regex

### Functions

- <h3><a id='extract-field-value'></a><span style="color:green">extract-field-value</span> [field body]<br></h3>
  gets value from the body using field ks, converting uuid's from string to UUID if needed <br>
  <br>
  - field ^ks : field to be extracted from a map <br>
  - body ^map : map where the field will be extracted <br>
  - returns ? : any value from the map <br>
```clojure
    (extract-field-value :name {:name "Rosa"})
    ;=> "Rosa"
```
```clojure
    (extract-field-value :id {:id "53bd29d3-9b41-4550-83cc-f970d49da04d"}) 
    ;=> #uuid "53bd29d3-9b41-4550-83cc-f970d49da04d"
```

- <h3><a id='is-uuid'></a><span style="color:green">is-uuid</span> [id]<br></h3>
  if id param is a string, checks if it matches uuid regex, otherwise returns false <br>
  <br>
  - id ^string : string to be checked against [uuid pattern](#uuid-pattern) <br>
  - returns ^boolean : if is a string and an uuid or not <br>
```clojure
    (is-uuid "53bd29d3-9b41-4550-83cc-f970d49da04d") ;=> true
```

- <h3><a id='mop-fields'></a><span style="color:green">mop-fields</span> [body fields]<br></h3>
  Clean the body removing values not present in fields param <br>
  <br>
  - body ^map : map to be cleaned <br>
  - fields [^string]: string collection with the name of the allowed fields in the map <br>
  - opts [^opts-def?](#mop-fields-opts-def): optional options 
  - **returns** *^map* : cleaned map <br>
```clojure
    (mop-fields {:name "Rosa" :age 41} ["name"]) 
    ;=> {:name "Rosa"} 
```

- <h3><a id='uuid'></a><span style="color:green">uuid</span><br></h3>
  returns a new random UUID <br>
  <br>
  - returns ^uuid : a random uuid <br>
```clojure
    (uuid) ;=> #uuid "53bd29d3-9b41-4550-83cc-f970d49da04d"
```
- <h3><a id='uuid-as-string'></a><span style="color:green">uuid-as-string</span> [uuid]<br></h3>
  converts uuid into a string <br>
  <br>
  - uuid ^uuid : uuid to be converted to string <br>
  - returns ^string : uuid as a string <br>
```clojure
    (uuid-as-string (uuid)) ;=> "53bd29d3-9b41-4550-83cc-f970d49da04d"
```

- <h3><a id='validate-and-mop!!'></a><span style="color:green">validate-and-mop!!</span> [body mandatory accepted & field-message = "Field %s is not present"]<br></h3>
  Validates and clean body by executing [validate-mandatory](#validate-mandatory) and `mop-fields` <br>
  <br>
  - body ^uuid : uuid to be converted to string <br>
  - mandatory [^string] | [^validation-def](#validation-def) : either coll of strings or map following [^validation-def](#validation-def) specs. For coll of strings, mandatory validation is triggered by default, other validations require the map <br>
  - accepted [^string] : collection of strings having accepted keys on body, the others will be removed <br>
  - field-message ^string? : optional argument to customize message, used only when mandatory argument is coll of strings  <br>
  - returns ^map : filtered and validated body <br>
  - throws ^ExceptionInfo : exception info with data having bad format type and validation-messages for each field <br>
```clojure
   (validate-and-mop!! {:name "Rosa" :extra 6} ["name"] ["name"])
   ;=> {:name "Rosa"}
```
```clojure
   (validate-and-mop!! {:name "Rosa"} ["age"] ["name" "age"])
   ;=> ExceptionInfo thrown => ExceptionInfo{data {:type :bad-format
   ;                                               :validation-messages [{:field "age"
   ;                                                                      :message "Field :age is not present"}]}}
```
```clojure
   (validate-and-mop!! 
        {:name "Rosa" :age 17}
        {"age" [{:validate/type :validate/min, :validate/value 18}]}
        ["name" "age"])
   ;=> ExceptionInfo thrown => ExceptionInfo{data {:type :bad-format
   ;                                               :validation-messages [{:field "age"
   ;                                                                      :message "Field age must have a minimum size of 18"}]}}
```
