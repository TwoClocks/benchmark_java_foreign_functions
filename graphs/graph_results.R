
library(dplyr)
library(ggplot2)
library(tidyverse)
library(RColorBrewer)
library(grid)
library(extrafont)
library(stringr)
library(cowplot)

# font_import()

cpu_type <- "AMD Ryzen 5 5600G"
#cpu_type <- "AMD Ryzen 5 5600G"

find_maxy <- function( runs ) {
  rng <- runs %>% group_by( name ) %>% group_map( ~ median_hilow( . ) ) %>% 
    bind_rows()
  ymin <- min(rng$ymin)
  ymax <- max(rng$ymax)
  ret <- ((ymax-ymin)*.01) + ymax
  cat(ymin,ymax,ret,"\n")
  ret
}

find_best <- function(glob,name) {
  fl <- Sys.glob(glob)
  fl <- as.data.frame(fl)
  names(fl) <- c("file_name")
  fl$data <- map(fl$file_name, ~ load_data(.,name))
  v <- bind_rows( map(fl$data,~median_hilow(.$sample)) )
  fl$data[[which.min(v$ymax)]]
}

load_data <- function(fileName,name) {
  fdata <- read.csv2(fileName,header = TRUE, sep=",")
  fdata <- select(fdata,sample_measured_value,unit,iteration_count)
  names(fdata) <- c("sample","unit","ittr_num")
  fdata$sample <- as.numeric(fdata$sample)
  fdata$sample <- fdata$sample / fdata$ittr_num
  fdata$name <- name
  select(fdata,name,sample)
}


graph_data <- function(pdata,lang_name,max_y) {
  
  cat(paste(lang_name," mx: ",max_y,"\n"))
  
  tmp <- str_replace(lang_name,"\\+\\+","pp")
  fname <- paste( tolower(tmp), "-", word(cpu_type,1),".png",sep="")
  
  write(paste("\n fanme = ",fname, "\n"),stdout())
  
  png(fname,res=150,width=800, height=400)
  
  p1 <- ggplot(pdata, aes(x=name, y=sample, fill=name, color=name)) +
    geom_violin(show.legend = FALSE,width=1.5) +
    coord_flip() +
    scale_fill_brewer(type="div",palette="Dark2") +
    scale_colour_brewer(type="div",palette="Dark2") +
    #stat_summary(fun.data = mean_sdl, fun.args=list(mult=1), geom="crossbar",width=0.08,fill=NA,color="black") +
    stat_summary(fun.data = median_hilow, geom='errorbar', color="black", width=.3) +
    stat_summary(fun = "mean", geom="point",size=2,shape=23,fill="white",color="black") +
    theme(plot.title = element_text(hjust = 0.5),plot.subtitle = element_text(hjust = 0.5), text = element_text(size = 10)) +
    labs(title=paste(lang_name, " round-trip times"), y="Nanoseconds",x=element_blank(),subtitle=cpu_type) +
    ylim(NA,max_y)

  p1 <- add_sub(p1, "whisker lines are 95% confidence interval. white diamonds are mean", size=6)
  ggdraw(p1)
  
}

jnaFunc <- find_best("../runs/janFunc*.csv","JNA_Func")
unsafeMem <- find_best("../runs/unsafe*.csv","Unsafe_Mem")
forFunc <- find_best("../runs/foreignFunc*.csv","Foreign_Func")
forMem <- find_best("../runs/ForeignMem*.csv","Foreign_Mem")

# all <- rbind( janFunc, unsafeMem, forFunc, forMem )
all <- rbind( unsafeMem, forFunc, forMem )

graph_data( all, "benches", find_maxy( all ) )

graph_data( jnaFunc, "JNA", find_maxy( jnaFunc ) )

dev.off()


