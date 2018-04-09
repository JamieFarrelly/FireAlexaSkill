Fire Alexa
================================

Alexa Skill to check your business Fire account balances or move money between two of your Fire accounts.

Overview
--------------------------
Project that's based off [Amazon's sample projects](https://github.com/amzn/alexa-skills-kit-java) so that you can ask for the likes of
"Alexa, ask Fire to check my balance" or "Alexa, ask Fire to move 10 euro from first account to second account". See an example of the balance check [here](https://twitter.com/Jamie_Farrelly/status/814134723085791232).

To get started
--------------------------

* You'll need a Fire business account, [you can sign up here](https://fire.com)
* Create an API application in [business.fire.com](https://business.fire.com) and make sure it has permission to get account details
* Clone this repo
* In FireApi.java change CLIENT_KEY, CLIENT_ID and REFRESH_TOKEN to be whatever your API application details are
* Deploy the skill, I used Lambda since it's the easiest way to get going quickly. [Follow Amazon's docs to deploy the skill](https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/deploying-a-sample-skill-to-aws-lambda)
* Make sure the skill is set to whatever language your device (Echo, Echo Dot etc.) uses, the default is US English but this won't work if yours is set up for UK English

Why you have to deploy the skill yourself
--------------------------
There's no reason why this skill couldn't be available to everyone and be up on the Alexa Skills Store, but there's a bit to think about first...you'd have to store people's CLIENT_KEYs etc. and store them securly since they could be used to make payments in the future, not just read only access. Because of this, I've kept it to work with just one account but feel free to clone the repo and use it yourself.
