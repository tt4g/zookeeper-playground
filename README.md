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
