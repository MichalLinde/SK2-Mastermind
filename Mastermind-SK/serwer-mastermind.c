#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <pthread.h>

struct cln {
	int cfd;
	struct sockaddr_in caddr;
};

void *th(void *arg) {
	int gameRunning = 1;
    int *fds = (int*) arg;
    int c1 = fds[0];
    int c2 = fds[1];
	char buffer[8];
            
    int length = 7, n = 0;
    while (length > 0){
            n = write(c2, "111111\n", 7);
            length = length - n;
    }


    length = 7;
    n = 0;
    while (length > 0){
        n = write(c1, "000000\n", 7);
        length = length - n;
    }
    printf("obie poszly\n");
    
    buffer[7] = '\n';

    //GLOWNA PETLA ROZGRYWKI
    while (gameRunning == 1){
        
        //odebranie wiadomosci od gracza szyfrujacego
        length = 7;
        n = 0;
        int received = 0;
        int receiving = 1;
        printf("przed odebraniem szyfr\n");
        while (receiving == 1){
                n = read(c2, buffer + received, length - received);
                printf("odczytano %d\n", n);
                if (n <= 0){
                    break;
                } else{
                    received = received + n;
                    printf("w trakcie odb szyfr\n");
                }
        }  

        //wysylanie wiadomosci do gracza odgadujacego
        length = 8;
        n = 0;
        int sent = 0;
        int sending = 1;
        printf("przed wysylaniem odg\n");
        while (sending == 1){
                n = write(c1, buffer + sent, length - sent);
                printf("wyslano %d\n", n);
                if (n <= 0){
                    break;
                } else{
                    printf("wyslano: %d\n", n);
                    sent = sent + n;
                    printf("w trakcie wysylania odg\n");
                }
        } 

        //odebranie wiadomosci od gracza odgadujacego
        length = 7;
        n = 0;
        received = 0;
        receiving = 1;
        printf("przed odebraniem odg\n");
        while (receiving == 1){
                n = read(c1, buffer + received, length - received);
                printf("odczytano %d\n", n);
                if (n <= 0){
                    break;
                } else{
                    received = received + n;
                    printf("w trakcie odbierania odg\n");
                }
        }

        //sprawdzenie stanu gry (drugi znak wiadomosci opisuje stan gry, jezeli jest rowny 0 to znaczy ze gra dalej trwa)
        if (buffer[1] == '1'){
            gameRunning = 0;
            printf("Sprawdzenie stanu gry: %c\n", buffer[1]);
        }

        //wysylanie wiadomosci do gracza szyfrujacego
        length = 8;
        n = 0;
        sent = 0;
        sending = 1;
        printf("przed wyslaniem szyfr\n");
        while (sending == 1){
                n = write(c2, buffer + sent, length - sent);
                printf("wyslano %d\n", n);
                if (n <= 0){
                    break;
                } else{
                    sent = sent + n;
                    printf("w trakcie wysylania szyfr\n");
                }
        }
        printf("Po wyslaniu do szyfrujacego\n"); 
    }

    close(c1);          
    close(c2);

  printf("Koniec rozgrywki!\n");
  return EXIT_SUCCESS;

}

int clients[2];

int main(int argc, char * argv[]){
    pthread_t tid;
    socklen_t slt;
    int sfd = socket(PF_INET, SOCK_STREAM, 0);
    struct sockaddr_in saddr;
    int inLobby = 0;
    
    

    int on = 1;
    setsockopt(sfd, SOL_SOCKET, SO_REUSEADDR, (char*) &on, sizeof(on));

    saddr.sin_family=PF_INET;
	saddr.sin_port=htons(1234);
	saddr.sin_addr.s_addr=INADDR_ANY;

    bind(sfd, (struct sockaddr*) &saddr, sizeof(saddr));

    listen(sfd, 16);

    while(1){
        struct cln* c = malloc(sizeof(struct cln));
        slt = sizeof(c->caddr);
        c->cfd = accept(sfd, (struct sockaddr*) &c->caddr, &slt);
        printf("New connection!\n");
        inLobby = inLobby + 1;
        clients[inLobby % 2] = c->cfd;
    

        if (inLobby % 2 == 0){
            pthread_create(&tid, NULL, th, clients);
            pthread_detach(tid);
    }
    }
    close(sfd);
    return EXIT_SUCCESS;

}