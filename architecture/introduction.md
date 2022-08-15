# Laputa Clouds Lib

Manage files in Google Drive, OneDrive, pDrive, Box, DropBox. Spiritual successor to CloudRail.

Allows for foss-only build, that allows skipping non opensource components, without losing features.

## Component graph

```mermaid
flowchart LR;

subgraph AbstractAccount
    direction LR
    BoxAccount;
    DropBoxAccount;
    GoogleAccount;
    OneDriveAccount;
    PhoneAccount;
end
BoxAccount     -->|Clouds.init|BoxDriver;
DropBoxAccount -->|Clouds.init|DropBoxDriver;
GoogleAccount  -->|Clouds.init|GoogleDriveDriver;
OneDriveAccount-->|Clouds.init|OneDriveDriver;
PhoneAccount   -->|Clouds.init|PhoneDriver;
subgraph AbstractFile
    a[List of];
    BoxFile;
    b[List of];
    DropBoxFile;
    c[List of];
    GoogleDriveFile;
    d[List of];
    OneDriveCloudFile;
    e[List of];
    PhoneFile;
end
subgraph AbstractPath
    BoxPath;
    DropBoxPath;
    GoogleDrivePath;
    OneDrivePath;
    PhonePath;
end
subgraph AbstractFileStructureDriver
    direction LR

    BoxDriver        --->|getRoot|BoxPath;
    DropBoxDriver    --->|getRoot|DropBoxPath;
    GoogleDriveDriver--->|getRoot|GoogleDrivePath;
    OneDriveDriver   --->|getRoot|OneDrivePath;
    PhoneDriver      --->|getRoot|PhonePath;

    BoxDriver        --->|getFiles|a;
    DropBoxDriver    --->|getFiles|b;
    GoogleDriveDriver--->|getFiles|c;
    OneDriveDriver   --->|getFiles|d;
    PhoneDriver      --->|getFiles|e;

    a --> BoxFile;
    b --> DropBoxFile;
    c --> GoogleDriveFile;
    d --> OneDriveCloudFile;
    e --> PhoneFile;

    BoxDriver        --->|getFile|BoxFile;
    DropBoxDriver    --->|getFile|DropBoxFile;
    GoogleDriveDriver--->|getFile|GoogleDriveFile;
    OneDriveDriver   --->|getFile|OneDriveCloudFile;
    PhoneDriver      --->|getFile|PhoneFile;

    BoxFile          -->|getParent|BoxFile;
    DropBoxFile      -->|getParent|DropBoxFile;
    GoogleDriveFile  -->|getParent|GoogleDriveFile;
    OneDriveCloudFile-->|getParent|OneDriveCloudFile;
    PhoneFile        -->|getParent|PhoneFile;

    BoxFile          --->|path|BoxPath;
    DropBoxFile      --->|path|DropBoxPath;
    GoogleDriveFile  --->|path|GoogleDrivePath;
    OneDriveCloudFile--->|path|OneDrivePath;
    PhoneFile        --->|path|PhonePath;
end
```

## Operation graph

```mermaid
sequenceDiagram
    participant App
    participant Lib Dispatchers.Main
    participant Lib Dispatchers.IO

    App->>Lib Dispatchers.Main: Clouds.init
    Lib Dispatchers.Main->>App: Account callback
    App->>Lib Dispatchers.Main: App calls operation
    Lib Dispatchers.Main->>Lib Dispatchers.IO: Lib executes operation
    activate Lib Dispatchers.IO
    Lib Dispatchers.IO->>Lib Dispatchers.Main: Result
    deactivate Lib Dispatchers.IO
    Lib Dispatchers.Main->>App: Lib calls result callback

```
