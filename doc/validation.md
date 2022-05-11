## Index
- Definitions
  - [validation-def](#validation-def)
- Symbols
- Functions
  - [validate](#validate)

## Definitions
Patterns for some maps passed to the functions as arguments:
- <h3><a id='validation-def'></a><span style="color:coral">validation-def</span> ^map</h3><br>
    - | key              | value                  | description                                              | mandatory extra key      | optional extra key                                                                 |
      | -----------------| -----------------------| -------------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------- |
      | `:validate/type` | `:validate/mandatory`  | validates if field is present                            | -                        | `:validate/message` ^string with %s being field name                               |
      | `:validate/type` | `:validate/max`        | validates if max is not reached                          | `:validate/value` ˆint   | `:validate/message` ^string with 1st %s being field name, 2nd being max value      |
      | `:validate/type` | `:validate/min`        | validates if at least min value is reached               | `:validate/value` ˆint   | `:validate/message` ^string with 1st %s being field name, 2nd being min value      |
      | `:validate/type` | `:validate/regex`      | validates if string matches regex pattern                | `:validate/value` ˆregex | `:validate/message` ^string with %s being field name                               |
      | `:validate/type` | `:validate/custom`     | validates if custom fn receiving the value returns true  | `:validate/value` ^fn    | `:validate/message` ^string with %s being field name                               |
    - Examples:
    ```clojure
      {"field-name"
        [{:validate/type :validate/mandatory, :validate/message "%s is ..."},
         {:validate/type :validate/min, :validate/value 12, :validate/message "%s is mandatory"},
         {:validate/type :validate/max, :validate/value 40, :validate/message "%s is ..."},
         {:validate/type :validate/regex, :validate/value #"^[\d]{1,2}$", :validate/message "%s is ..."},
         {:validate/type :validate/custom, :validate/value fn, :validate/message "% is ..."}]}
    ```


## Symbols


## Functions

- <h3><a id='validate'></a><span style="color:green">validate</span> [body fields]<br></h3>
  validates the body based on a map following [validation-def](#validation-def)<br>
  <br>
    - *body* ^map : map where the `field` will be extracted
    - *fields* ^map : map following [validation-def](#validation-def) to perform validations over the body
    - **returns** *boolean* : true if validations succeed
    - **throws** *^ExceptionInfo* : exception info with data having bad format type and validation-messages for each field
  ```clojure
   (validate 
        {:name "Rosa"} 
        {"age" [{:validate/type :validate/mandatory}]})
   ;=> ExceptionInfo thrown => ExceptionInfo{data {:type :bad-format
   ;                                               :validation-messages [{:field "age"
   ;                                                                      :message "Field age is not present"}]}}
  ```
  ```clojure
   (validate 
        {:name "Rosa" :age 17}
        {"age" [{:validate/type :validate/min, :validate/value 18}]})
   ;=> ExceptionInfo thrown => ExceptionInfo{data {:type :bad-format
   ;                                               :validation-messages [{:field "age"
   ;                                                                      :message "Field age must have a minimum size of 18"}]}}
  ```