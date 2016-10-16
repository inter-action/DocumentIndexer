package github.interaction.docsearcher.utils

object ResouceUtil{
  type Resource = {def close(): Unit}

  def withResoure[T](resource: Resource)(f: (Resource)=>T): Either[Throwable, T] ={
    try {
      Right(f(resource))
    } catch {
      case e: Throwable =>
        try{
          resource.close()
        } catch {
          case ie: Throwable => return Left(ie)
        }
        Left(e)
    }
  }
}