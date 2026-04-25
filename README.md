[Apache ZooKeeper](https://zookeeper.apache.org/) Playground.

## Containers

### Launch containers

```shell
$ docker compose up -d
```

### ZooKeeper CLI

Using Zookeeper `zkCli.sh`.

```shell
$ docker compose exec zookeeper-playground-zookeeper-1 \
        bin/zkCli.sh

# List of nodes in zkCli.sh
$ ls /
# List of commands in zkCli.sh
$ ?
# Quit
$ quit

```

### ZooKeeper Commands

Send ZooKeeper commands to containers.

```shell
$ echo srvr | \
    docker compose exec zookeeper-playground-zookeeper-1 \
        nc localhost 2181
```

Allowed commands are listed in the environment variable
`ZOO_4LW_COMMANDS_WHITELIST` in [compose.yml](./compose.yml).

# Run Publisher and Consumer

```shell
# Start containers.
$ docker compose up -d

# Run Producer.
$ ./gradlew run --args="--mode producer"

# Run Consumer (another console).
$ ./gradlew run --args="--mode consumer"
```
