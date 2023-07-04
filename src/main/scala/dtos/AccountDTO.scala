package dtos

//import dtos.AccountDTO.toEntity
import entities.Account

case class AccountDTO(id: String, name: String, email: String){
  def toEntity(): Account = {
    Account(this.id, this.name, Some(this.email), Some(true))
  }
}

/*object AccountDTO {

  def toEntity(accountDTO: AccountDTO): Account = {
    Account(accountDTO.id, accountDTO.name, Some(accountDTO.email), Some(true))
  }
}*/
