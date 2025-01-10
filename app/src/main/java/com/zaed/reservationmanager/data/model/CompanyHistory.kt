package com.zaed.reservationmanager.data.model

data class CompanyHistory(
    val company: Company,
    val reservations: List<Reservation>,
    val payments: List<CompanyPayment>,
)
fun convertToCompanyHistoryList(
    reservations: Map<String, List<Reservation>>,
    payments: Map<String, List<CompanyPayment>>,
    companies: List<Company>
): List<CompanyHistory> {
    return companies.map { company ->
        CompanyHistory(
            company = company,
            reservations = reservations[company.id] ?: emptyList(),
            payments = payments[company.id] ?: emptyList()
        )
    }
}

fun List<CompanyHistory>.filterOpenAccountCompanies(companyType: CompanyType)=
    this.filter {
        it.reservations.sumOf {
            if(companyType == CompanyType.TRAVEL){
                it.travelRidePrice
            }else{
                it.tourismRidePrice
            }
        }.minus(
            it.reservations.sumOf {
                if(companyType == CompanyType.TRAVEL){
                    it.travelCollectedAmount
                }else{
                    it.tourismCollectedAmount
                }
            }
        ).minus(
            it.payments.sumOf {it.amount}.toInt()
        ) != 0
    }.sortedBy { it.company.name }