# balls_distribution
project to distributed systems class



Temos dez threads que vai armazenar um array do tipo {color: string, xPos: number, yPos: number } []


{color: string, xPos: number, yPos: number } Esse tipo representa um jogador. Cada thread vai mudar sua posição constantemente e o objetivo desse software é 

- Implementar um algoritmo de consenso que mantenha o array sincronizado em todas as máquinas
- Implementar um motor de vizualização da posição de todos os pontos de vista, de modo que seja possível clicar numa máquina e ver como ela está no jogo

