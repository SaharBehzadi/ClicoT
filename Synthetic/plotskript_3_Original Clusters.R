setwd("C:/Users/saharb63cs/workspace/Original_clico_T_engl_v11/Synthetic")

getRed<- function(idx){

r <- colors[[1]][idx+1]
return (r/255)

}

getGreen<- function(idx){

g <- colors[[2]][idx+1]
return (g/255)

}


getBlue<- function(idx){

b <- colors[[3]][idx+1]
return (b/255)

}

colors = read.csv("50_colors.csv")



files <- list.files(path = "C:/Users/saharb63cs/workspace/Original_clico_T_engl_v11/Synthetic/Plot", pattern = NULL, all.files = FALSE,
           full.names = FALSE, recursive = FALSE,
           ignore.case = FALSE, include.dirs = FALSE, no.. = FALSE)


fileOnePath <- paste("Plot/",files[1],sep="")
data2 = read.csv(fileOnePath)
plot(data2$Numerical_1,data2$Numerical_2,xlab="x",ylab="y",col=rgb(1,0,0))
limits <-par("usr")


for(i in 1:length(files)){
chars <- "split";

pathstring  <- paste("Plot/",files[i],sep="")


data = read.csv(pathstring)


pdf(file=paste("C:/Users/saharb63cs/workspace/Original_clico_T_engl_v11/Synthetic/Plot/",files[i],".pdf",sep=""))


if(grepl(chars,pathstring)){

 plot(data2$Numerical_1,data2$Numerical_2,main="Original Dataset",xlab="Numerical Dimension 1",ylab="Numerical Dimension 2",xlim=c(limits[1],limits[2]),ylim=c(limits[3],limits[4]),col=rgb(0.1,0.1,0.1,alpha=0.1))
 points(data$Numerical_1,data$Numerical_2,main="Original Dataset",xlab="Numerical Dimension 1",ylab="Numerical Dimension 2",xlim=c(limits[1],limits[2]),ylim=c(limits[3],limits[4]),col=rgb(getRed(data$color),getGreen(data$color),getBlue(data$color),alpha=1))
}


plot(data$Numerical_1,data$Numerical_2,main="Original Dataset",xlab="Numerical Dimension 1",ylab="Numerical Dimension 2",xlim=c(limits[1],limits[2]),ylim=c(limits[3],limits[4]),col=rgb(getRed(data$color),getGreen(data$color),getBlue(data$color),alpha=1))


dev.off()

}








