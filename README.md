# java-explore-with-me
Repository for diploma project.

    ██████╗██╗  ██╗█████╗ ██╗   █████╗  █████╗ ██████╗ ██╗     ██╗██╗ ██████╗██╗  ██╗ ███╗   ███╗██████╗
    ██╔═══╝╚██╗██╔╝██╔═██╗██║   ██╔══██╗██╔═██╗██╔═══╝ ██║  █╗ ██║╚╝║ ╚═██╔═╝██║  ██║ ████╗ ████║██╔═══╝
    ████╗   ╚███╔╝ █████╔╝██║   ██║  ██║█████╔╝████╗   ╚██╗██╗██╔╝██║   ██║  ███████║ ██╔████╔██║████╗
    ██╔═╝   ██╔██╗ ██╔══╝ ██║   ██║  ██║██╔═██╗██╔═╝    ███╔═███║ ██║   ██║  ██╔══██║ ██║╚██╔╝██║██╔═╝
    ██████╗██╔╝╚██╗██║    █████╗╚█████╔╝██║ ██║██████╗  ╚█╔╝ ╚█╔╝ ██║   ██║  ██║  ██║ ██║ ╚═╝ ██║██████╗
    ╚═════╝╚═╝  ╚═╝╚═╝    ╚════╝ ╚════╝ ╚═╝ ╚═╝╚═════╝   ╚╝   ╚╝  ╚═╝   ╚═╝  ╚═╝  ╚═╝ ╚═╝     ╚═╝╚═════╝

Service for sharing information about interesting events and finding a company to participate in them.

Statistics service database schema:

```mermaid
classDiagram
direction BT
class hits {
   varchar(512) application
   varchar(512) uri
   varchar(255) remote_ip
   timestamp date_time
   bigint id
}
```