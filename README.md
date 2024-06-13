# OracleJavaBot25
A repository for full stack Cloud Native applications with a React JS frontend and various backends (Java, Python, DotNet, and so on) on the Oracle Cloud Infrastructure. 

# TO RUN
```
git clone https://github.com/BoJavs-svg/OracleJavaBot25.git
```

```
cd OracleJavaBot25/MtdrSpring/
chmod +x *.sh
echo source $(pwd)/env.sh >> ~/.bashrc
source env.sh
source setup.sh
```

Sure! Here's a comprehensive README file for your GitHub repository based on your user manual:

---

# Oracle Task Master

Oracle Task Master is a Telegram bot designed to streamline task management and enhance productivity. This README provides comprehensive instructions for developers and users on how to install, set up, and use the bot effectively.

## Table of Contents
- [Introduction](#introduction)
- [Installation](#installation)
  - [For Developers](#for-developers)
    - [Getting Started](#getting-started)
    - [Setup the Development Environment](#setup-the-development-environment)
    - [Frontend (Telegram Bot)](#frontend-telegram-bot)
    - [Backend (Java/Spring Boot)](#backend-java-spring-boot)
  - [For Users](#for-users)
- [Usage](#usage)
  - [User Commands](#user-commands)
  - [Team Commands](#team-commands)
  - [Sprint Commands](#sprint-commands)
  - [Task Commands](#task-commands)

## Introduction

Welcome to the Oracle Task Master! This bot helps you manage your task lists on Telegram, whether you're a developer juggling multiple projects or a manager overseeing various tasks. This guide covers everything from installation to usage of all the bot's features.

## Installation

### For Developers

#### Getting Started

The development of this bot is based on the [Oracle workshop](https://apexapps.oracle.com/pls/apex/r/dbpm/livelabs/run-workshop?p210_wid=3701&p210_wec=&session=1183742264263).

**Prerequisites:**
- Oracle Cloud Account Name
- Username
- Password

#### Setup the Development Environment

**Prerequisites:**
- Oracle Cloud account

1. **Create Group and Appropriate Policies**
    - Navigate to Identity & Security > Groups > Create Group
    - Add your user to the group
    - Create policies for the group
    ```shell
    Allow group myToDoGroup to use cloud-shell in tenancy
    Allow group myToDoGroup to manage users in tenancy
    Allow group myToDoGroup to manage all-resources in tenancy
    Allow group myToDoGroup to manage buckets in tenancy
    Allow group myToDoGroup to manage objects in tenancy
    ```

2. **Launch the Cloud Shell**
    - Access it through the OCI Console by clicking the Cloud Shell icon.

3. **Create a Folder for the Workshop Code**
    ```shell
    mkdir reacttodo
    cd reacttodo
    ```

4. **Clone the Workshop Code**
    ```shell
    git clone https://github.com/BoJavs-svg/OracleJavaBot25.git
    cd OracleJavaBot25/MtdrSpring/
    chmod +x *.sh
    echo source $(pwd)/env.sh >> ~/.bashrc
    ```

5. **Start the Setup**
    ```shell
    source env.sh
    source setup.sh
    ```

6. **Monitor the Setup**
    - The setup should take around 20 minutes.

7. **Complete the Setup**
    ```shell
    ls -al $MTDRWORKSHOP_LOG
    ```

#### Frontend (Telegram Bot)

1. **Understand the Telegram Bot GUI**
    - The main screen has one input field and four buttons.

2. **Understand the Telegram Bot Implementation**
    - The bot uses the Telegram Bot API with the "long pooling method" for managing interactions.

3. **Create a Telegram Bot**
    - Use the BotFather on Telegram to create your bot and get the token ID.

#### Backend (Java/Spring Boot)

1. **Build and Push the Docker Images to the OCI Registry**
    ```shell
    cd $MTDRWORKSHOP_LOCATION/backend
    source build.sh
    ```

2. **Deploy on Kubernetes and Check the Status**
    ```shell
    cd $MTDRWORKSHOP_LOCATION/backend
    ./deploy.sh
    ```

### For Users

1. **Prerequisites**
    - Telegram installed and updated
    - A Telegram account

2. **Open the Telegram App**
    - Search for `@TaskMaster25000_bot` and select "Oracle Task Master".

3. **Start the Bot**
    - Type `/start` to create an account.

## Usage

This section provides a step-by-step guide on how to use the bot's functions. Commands are prefixed with `/`.

### User Commands 

- **Edit User**: `/edituser` - Edit your user information (name/role).

### Team Commands (for Managers)

- **Create Team**: `/createteam` - Provide team name and description.
- **Edit Team**: `/editteam` - Provide team ID, new name, and new description.
- **Delete Team**: `/deleteteam` - Provide team ID.
- **View Team Members**: `/viewteammembers` - Provide team ID.
- **View Team Tasks**: `/viewteamtasks` - Provide team ID.

### Sprint Commands

- **Create Sprint**: `/createsprint` - Provide sprint title, status, start and end dates. Managers also provide team ID.
- **View Sprint Tasks**: `/viewsprinttasks` - View tasks for a selected sprint.
- **Edit Sprint**: `/editsprint` - Edit sprint information by name.
- **Delete Sprint**: `/deletesprint` - Delete a sprint by name (deletes all associated tasks).

### Task Commands

- **Create Task**: `/addtask` - Provide description and status, then select a sprint.
- **View All User Tasks**: `/mytask` - View all your tasks.
- **Edit Task**: `/edittask` - Select a task to change its description, status, or sprint.
- **Finish Task**: `/finishtask` - Mark a task as finished (does not delete it).

---

Feel free to modify this README as needed for your specific requirements or any additional details you'd like to include.
