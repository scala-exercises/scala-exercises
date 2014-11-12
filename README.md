#Doing Scala

------------------------

##How it works?

"Doing Scala" offers hundreds of exercises organized in 42 categories whose challenge consists of resolve them, covering main concepts of the language.


- LEARN: Each category is preceded by an explanation of the basics. Learn the concepts through simple examples of code.

- SOLVE: Each exercise is a unit test that must be passed successfully, filling blanks. Instantly you can to know if you got it correctly since evaluation of them is performed in real time.

- SHARE: The system will consider the category as completed when all its exercises were successfully done. Don't forget share your progress on social networks before jump to the next category!

- EDIT: After completing a category, you'll be able to edit it. Adding new exercises or improving existing ones just sending a pull-request.

- APPROVED: Complete every category and you'll get our Scala ninja certificate.


##How build and deploy the app

```bash
grunt build
git add dist && git commit -m "message"
git push dist origin master
grunt buildcontrol:pages
```
