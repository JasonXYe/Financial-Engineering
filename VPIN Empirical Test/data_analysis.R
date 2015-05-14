library(xlsx)
library(forecast)
data=read.xlsx("/Users/zhedong/Downloads/SEMI_good.xlsx",1)
n=10
attach(data)
k=nrow(data)
data$abs_return=matrix(0,k,1)
data$vpin_lag=matrix(0,k,1)
for (a in 2:k){
  data$abs_return[a]=PX_LAST[a]/data$PX_LAST[a-1]-1
}
for (b in 2:k){
data$vpin_lag[b]=vpin[b-1]
}
m=nrow(data$abs_return)
cor(data$abs_return,data$vpin_lag)
cor(data$vpin,data$PX_LAST)
data$index1=as.numeric(cut(data$vpin_lag,n))
data$index2=as.numeric(cut(data$abs_return,n))
table=table(data$index1,data$index2)
prop.table(table)
acf(data$vpin)
acf(diff(data$vpin))
pacf(diff(vpin))
model=arima(vpin,order = c(3,1,0))
model
auto.arima(vpin,1,max.p=4,max.q=4)
fit=Arima(vpin,order=c(1,1,1))
plot(fit$x,col="red")
lines(fitted(fit),col="blue")
plot(vpin,type="l")

