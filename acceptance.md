## Lectures

**given:** A Folder with a *lecture.conf* file  
**when:** There is/are one or mulitple *\*.questions.(md|txt)* file(s) in the same or in sub-folders  
**then:** only those questions should be asked

**given:** multiple Folders on the same level with some *lecture.conf* files  
**when:** they have *\*.questions.(md|txt)*  
**then:** they are separate lectures

## *\*.question.(md|txt)* files

> Each file must start with a topic, denoted by one or up to six hashtags "#" followed by one or multiple questions.  
> Questions are separated by at least 2 consecutive *Newline*  characters
>
> there can be multiple empty lines between questions and a new

**given:** a file  
**when:** it starts with a heading  
**then:** all the questions under that heading until the next heading are parsed

**given:** a file  
**when:** it does not start with a heading  
**then:** the user gets an Error Message with the file name

## Learning Interval

> each Question should asket **at least** 4 times before an Exam Date that is given in the corresponding Lecture  
> The intervalls should correspond to this [article](https://blog.alexanderfyoung.com/how-to-remember-what-you-learn-for-longer-with-spaced-repetition/ "Link to Article")  
> also look into [pm2](https://www.supermemo.com/en/archives1990-2015/english/ol/sm2), the one that anki uses

## Color Coding (Retrospective Timetable)

> User should be able to get an overview of the topics learned with dates and difficulty. aka Retrospective Timetable.

## Interleaving

> When studying multiple topics mix them (don't do this select all topics then practice all physics
> first, practice for a *time* physics and then change the subject)

## Difficulty

> maybe like Anki (again, good, easy) 