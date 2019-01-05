#!/bin/bash
usage(){
    echo -e "use this tool such as following:\n"
    echo -e "\tclear all"
    echo -e "\tsh build-images.sh -C\n"
    echo -e "\tall modules delfault version: 1.2.0"
    echo -e "\tsh build-images.sh -cb\n"
    echo -e "\tdelete build all modules"
    echo -e "\tsh build-images.sh -db -v 1.2.0\n"
    echo -e "\tdelete build amdin server v1.2.0"
    echo -e "\tsh build-images.sh -db admin -v 1.2.0\n"
    echo -e "\tredownload source zip file"
    echo -e "\tsh build-images.sh -db admin config -v 1.2.0 --completely"
}
help(){
    echo "Usage: build-images OPTIONS"
    echo 
    echo "a shell tool for build apollo docker image"
    echo
    echo "OPTIONS:"
    echo -e "\t-h\t--help,this help message."
    echo -e "\t-v\t--version,the apollo version should be used" 
    echo -e "\t-d\t--delete delete local docker images and jar files."
    echo -e "\t-c\t--check check local jar files ware downloaded."
    echo -e "\t-b\t--build build docker images,default execute check before running."
    echo -e "\t-C\t--clear clear all about apollo(all version and files)"
    echo -e "\t-a\t--all,clean all include apollo zip files,if there nothing disappears after this option means clean all modules include admin、config and portal " 
    echo
    echo "report bugs to <hilin2333@gmail.com>."
    exit 1
}
if [[ $# -eq 0 ]];then
    usage
    exit
elif [ "$1" = "-h" -o "$1" = "--help" ];then
    help
fi

# envoriment check
if [[ $(which docker|wc -l) -ne 1 ]] && [[ $(which wget|wc -l) -ne 1 ]] && [[ $(which unzip|wc -l) -ne 1 ]];then
    echo "docker、wget and unzip must be installed!"
else     
    echo "envoriment checked success"
fi

getopt --test 
if [ "$?" != "4" ];then 
    if [ "$(uname)" == "Darwin" ]; then
        brew -v > /dev/null 
        if [ "$?" != "0" ];then 
            echo "Please install brew for Mac: ruby -e '$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)'"
        else 
            echo "hhah$?"
            echo 'Please install gnu-getopt for Mac: brew install gnu-getopt' 
            exit 1 
        fi
    fi
fi 


#-o或--options选项后面接可接受的短选项，如ab:c::，表示可接受的短选项为-a -b -c，其中-a选项不接参数，-b选项后必须接参数，-c选项的参数为可选的
#-l或--long选项后面接可接受的长选项，用逗号分开，冒号的意义同短选项。
#-n选项后接选项解析错误时提示的脚本名字

ARGS=$(getopt -o aCcdv:b:: --long all,check,delete,clear,version:,build:: -n 'build-images.sh' -- "$@")

#将规范化后的命令行参数分配至位置参数（$1,$2,...)
eval set -- "${ARGS}"

clean=0
check=0
build=0
clear=0
admin=0
config=0
portal=0
APOLLO_VERSION=1.2.0
completely=0
while true
do
    case "$1" in
        -d|--delete)
        shift
        clean=1
        ;;
        -c|--check)
        shift
        check=1
        ;;
        -b|--build)
        shift
        build=1
        ;;
        -C|--clear)
        shift
        clear=1
        ;;
        admin)
        shift
        admin=1
        ;;
        config)
        shift
        config=1
        ;;
        portal)
        shift
        portal=1
        ;;
        -v|--version)
        shift
        regx="^[1-9]{1,}\.[1-9]{1,}\.[0-9]{1,}.*"
        if [[ "$1" =~ $regx ]]; then
            APOLLO_VERSION="$1"
            echo "current version is $1"
            shift
        else
            echo "shell parameters parse error, version not found!"
            exit 1   
        fi
        ;;
        -a|--all)
        shift
        completely=1
        ;;
        --)
        shift
        ;;
        "")
        shift
        ;;
    esac
    if [ $# -eq 0 ];then
        break
    fi
done

check(){
    module=$1
    if [[ $module = "admin" ]];then
        if [[ $(ls ./apollo-admin-server|grep apollo-adminservice.jar|wc -l) -ne 1 ]];then
            echo "cannot found apollo-adminservice.jar."
        fi    
    elif [[ $module = "config" ]];then
        if [[ $(ls ./apollo-config-server|grep apollo-configservice.jar|wc -l) -ne 1 ]];then
            eho "cannot found apollo-configservice.jar."       
        fi
    else   
        if [[ $(ls ./apollo-portal-server|grep apollo-portal.jar|wc -l) -ne 1 ]];then
            echo "cannot found apollo-portal.jar."     
        fi    
    fi
}
clean(){
    module=$1
    echo "clean $module"
    if [[ $module = "admin" ]];then
        rm -f ./apollo-admin-server/apollo-adminservice.jar
        if [[ $completely -eq 1 ]];then
            rm -f apollo-adminservice-$APOLLO_VERSION-github.zip
        fi
    elif [[ $module = "config" ]];then
        rm -f ./apollo-config-server/apollo-configservice.jar
        if [[ $completely -eq 1 ]];then
            rm -f apollo-configservice-$APOLLO_VERSION-github.zip
        fi
    else 
        rm -f ./apollo-portal-server/apollo-portal.jar
        if [[ $completely -eq 1 ]];then
            rm -f apollo-portal-$APOLLO_VERSION-github.zip
        fi
    fi
}

downloadAndUnzipAndRename(){
    module=$1
    if [[ $module = "admin" ]];then
        if [[ $(ls ./|grep -e apollo-adminservice-$APOLLO_VERSION-github.zip$|wc -l) -ne 1 ]];then
            wget https://github.com/ctripcorp/apollo/releases/download/v$APOLLO_VERSION/apollo-adminservice-$APOLLO_VERSION-github.zip
        fi
        unzip apollo-adminservice-$APOLLO_VERSION-github.zip apollo-adminservice-$APOLLO_VERSION.jar -d ./apollo-admin-server/ && \
        mv ./apollo-admin-server/apollo-adminservice-$APOLLO_VERSION.jar ./apollo-admin-server/apollo-adminservice.jar
    elif [[ $module = "config" ]];then
        if [[ $(ls ./|grep -e apollo-configservice-$APOLLO_VERSION-github.zip$|wc -l) -ne 1 ]];then
            wget https://github.com/ctripcorp/apollo/releases/download/v$APOLLO_VERSION/apollo-configservice-$APOLLO_VERSION-github.zip
        fi
        unzip apollo-configservice-$APOLLO_VERSION-github.zip apollo-configservice-$APOLLO_VERSION.jar -d ./apollo-config-server/ && \
        mv ./apollo-config-server/apollo-configservice-$APOLLO_VERSION.jar ./apollo-config-server/apollo-configservice.jar
    else
        if [[ $(ls ./|grep -e apollo-portal-$APOLLO_VERSION-github.zip$|wc -l) -ne 1 ]];then
            wget https://github.com/ctripcorp/apollo/releases/download/v$APOLLO_VERSION/apollo-portal-$APOLLO_VERSION-github.zip
        fi
        unzip apollo-portal-$APOLLO_VERSION-github.zip apollo-portal-$APOLLO_VERSION.jar -d ./apollo-portal-server/ && \
        mv ./apollo-portal-server/apollo-portal-$APOLLO_VERSION.jar ./apollo-portal-server/apollo-portal.jar
    fi
}

build(){
    module=$1
    if [[ $module = "admin" ]];then
        echo "starting build apollo-admin-server..."
        docker build -t apollo-admin-server:v$APOLLO_VERSION ./apollo-admin-server/
        echo "build apollo-admin-server success"
    elif [[ $module = "config" ]];then
        echo "starting build apollo-config-server..."
        docker build -t apollo-config-server:v$APOLLO_VERSION ./apollo-config-server/
        echo "build apollo-config-server success"
    else
        echo "starting build apollo-portal-server..."
        docker build -t apollo-portal-server:v$APOLLO_VERSION ./apollo-portal-server/
        echo "build apollo-portal-server success"
    fi   
}

cmd(){
    if [[ $clear -eq 1 ]];then
        rm -f ./apollo-admin-server/apollo-adminservice.jar ./apollo-config-server/apollo-configservice.jar ./apollo-portal-server/apollo-portal.jar
        rm -f $(ls|grep -e '^apollo-.*.zip.*$')
        echo "clear successful"
    fi
    modules=$1
    for i in ${modules[@]}; do
        if [[ $clean -eq 1 ]];then
            clean $i
        fi
        if [[ $check -eq 1 ]];then
            check $i
        fi
        if [[ $build -eq 1 ]];then
            downloadAndUnzipAndRename $i
            check $i
            build $i
        fi
    done
}
if [[ $admin -eq 0 && $config -eq 0 && $portal -eq 0 ]];then
    modules=("admin" "config" "portal")
    cmd "${modules[*]}"
fi    
if [[ $admin -eq 1 ]];then
    modules=("admin")
    cmd "${modules[*]}"
fi   
if [[ $config -eq 1 ]];then
    modules=("config")
    cmd "${modules[*]}"
fi        
if [[ $portal -eq 1 ]];then
    modules=("portal")
    cmd "${modules[*]}"
fi
