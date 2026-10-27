package com.yadyar.app.ui.member

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yadyar.app.YadyarApp
import com.yadyar.app.data.model.Member
import com.yadyar.app.data.model.MemberDetails
import com.yadyar.app.data.model.MemberType
import com.yadyar.app.data.repository.MemberDetailsRepository
import com.yadyar.app.data.repository.MemberRepository
import kotlinx.coroutines.launch

/**
 * وضعیت فرم مشخصات — همان فیلدهای «فرم مادر».
 */
data class MemberDetailsForm(
    var fatherName: String = "",
    var nationalId: String = "",
    var nationalIdSerial: String = "",
    var birthDate: String = "",
    var birthPlace: String = "",
    var certNo: String = "",
    var certSerial: String = "",
    var postalCode: String = "",
    var landlinePhone: String = "",
    var sanaStatus: String = "",
    var digitalBranch: String = "",
    var currentBranch: String = "",
    var digitalSignature: String = "",
    var checkCertSymbol: String = "",
    var tokenStatus: String = "",
    var creditReportUri: String = "",
    var balanceStatus: String = "",
    var shenasnamehPhotoUri: String = "",
    var nationalCardPhotoUri: String = "",
    var employmentCertPhotoUri: String = "",
    var payslipPhotoUri: String = "",
    var extraDocsUris: String = "",
    var phone: String = "",
    var operatorName: String = "",
    var simPuk: String = "",
    var accountNo: String = "",
    var cardNo: String = "",
    var cardExp: String = "",
    var cardCvv2: String = "",
    var cardPassword: String = "",
    var username: String = "",
    var password: String = "",
    var creditScore: String = "",
    var checkStatus: String = "",
    var errors: String = "",
    var freeCapacity: String = "",
    var activeLoans: String = "",
    var takenLoans: String = "",
    var collateral: String = ""
)

class MemberFormViewModel(
    private val memberId: String?,
    private val folderId: String?,
    private val memberRepository: MemberRepository,
    private val detailsRepository: MemberDetailsRepository
) : ViewModel() {

    val isEdit: Boolean = memberId != null

    var name by mutableStateOf("")
    var type by mutableStateOf(MemberType.CUSTOMER)
    var colorHex by mutableStateOf(Member.DEFAULT_COLORS[0])
    var avatarImageUri by mutableStateOf<String?>(null)
    var notes by mutableStateOf("")
    var nameError by mutableStateOf(false)
    var loaded by mutableStateOf(false)

    var details by mutableStateOf(MemberDetailsForm())

    private var existingDetailsId: String? = null

    init {
        if (memberId != null) {
            viewModelScope.launch {
                memberRepository.getById(memberId)?.let { m ->
                    name = m.name
                    type = m.typeEnum
                    colorHex = m.avatarColor
                    avatarImageUri = m.avatarImageUri
                    notes = m.notes
                }
                detailsRepository.getByMember(memberId)?.let { d ->
                    existingDetailsId = d.id
                    details = MemberDetailsForm(
                        fatherName = d.fatherName,
                        nationalId = d.nationalId,
                        nationalIdSerial = d.nationalIdSerial,
                        birthDate = d.birthDate,
                        birthPlace = d.birthPlace,
                        certNo = d.certNo,
                        certSerial = d.certSerial,
                        postalCode = d.postalCode, landlinePhone = d.landlinePhone, sanaStatus = d.sanaStatus,
                        digitalBranch = d.digitalBranch, currentBranch = d.currentBranch,
                        digitalSignature = d.digitalSignature, checkCertSymbol = d.checkCertSymbol,
                        tokenStatus = d.tokenStatus, creditReportUri = d.creditReportUri, balanceStatus = d.balanceStatus,
                        shenasnamehPhotoUri=d.shenasnamehPhotoUri, nationalCardPhotoUri=d.nationalCardPhotoUri, employmentCertPhotoUri=d.employmentCertPhotoUri, payslipPhotoUri=d.payslipPhotoUri, extraDocsUris=d.extraDocsUris,
                        phone = d.phone,
                        operatorName = d.operatorName,
                        simPuk = d.simPuk,
                        accountNo = d.accountNo,
                        cardNo = d.cardNo,
                        cardExp = d.cardExp,
                        cardCvv2 = d.cardCvv2,
                        cardPassword = d.cardPassword,
                        username = d.username,
                        password = d.password,
                        creditScore = d.creditScore,
                        checkStatus = d.checkStatus,
                        errors = d.errors,
                        freeCapacity = d.freeCapacity,
                        activeLoans = d.activeLoans,
                        takenLoans = d.takenLoans,
                        collateral = d.collateral
                    )
                }
                loaded = true
            }
        } else {
            loaded = true
        }
    }

    /** ذخیره عضو + مشخصات فرم. true = موفق */
    fun save(): Boolean {
        if (name.isBlank()) {
            nameError = true
            return false
        }
        nameError = false
        viewModelScope.launch {
            val trimmed = name.trim()
            val savedId: String
            if (memberId != null) {
                val old = memberRepository.getById(memberId) ?: return@launch
                savedId = old.id
                memberRepository.upsert(
                    old.copy(
                        name = trimmed,
                        type = type.key,
                        avatarColor = colorHex,
                        avatarImageUri = avatarImageUri,
                        avatarLetter = trimmed.take(1),
                        notes = notes.trim(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                val newMember = Member(
                    type = type.key,
                    name = trimmed,
                    avatarLetter = trimmed.take(1),
                    avatarColor = colorHex,
                    avatarImageUri = avatarImageUri,
                    notes = notes.trim()
                )
                savedId = newMember.id
                memberRepository.upsert(newMember)
            }

            // ذخیره مشخصات فرم
            detailsRepository.upsert(
                MemberDetails(
                    id = existingDetailsId ?: java.util.UUID.randomUUID().toString(),
                    memberId = savedId,
                    fatherName = details.fatherName.trim(),
                    nationalId = details.nationalId.trim(),
                    nationalIdSerial = details.nationalIdSerial.trim(),
                    birthDate = details.birthDate.trim(),
                    birthPlace = details.birthPlace.trim(),
                    certNo = details.certNo.trim(),
                    certSerial = details.certSerial.trim(),
                    postalCode = details.postalCode.trim(), landlinePhone = details.landlinePhone.trim(), sanaStatus = details.sanaStatus.trim(),
                    digitalBranch = details.digitalBranch.trim(), currentBranch = details.currentBranch.trim(),
                    digitalSignature = details.digitalSignature.trim(), checkCertSymbol = details.checkCertSymbol.trim(),
                    tokenStatus = details.tokenStatus.trim(), creditReportUri = details.creditReportUri.trim(), balanceStatus = details.balanceStatus.trim(),
                    shenasnamehPhotoUri=details.shenasnamehPhotoUri, nationalCardPhotoUri=details.nationalCardPhotoUri, employmentCertPhotoUri=details.employmentCertPhotoUri, payslipPhotoUri=details.payslipPhotoUri, extraDocsUris=details.extraDocsUris,
                    phone = details.phone.trim(),
                    operatorName = details.operatorName.trim(),
                    simPuk = details.simPuk.trim(),
                    accountNo = details.accountNo.trim(),
                    cardNo = details.cardNo.trim(),
                    cardExp = details.cardExp.trim(),
                    cardCvv2 = details.cardCvv2.trim(),
                    cardPassword = details.cardPassword.trim(),
                    username = details.username.trim(),
                    password = details.password.trim(),
                    creditScore = details.creditScore.trim(),
                    checkStatus = details.checkStatus.trim(),
                    errors = details.errors.trim(),
                    freeCapacity = details.freeCapacity.trim(),
                    activeLoans = details.activeLoans.trim(),
                    takenLoans = details.takenLoans.trim(),
                    collateral = details.collateral.trim()
                )
            )
        }
        return true
    }

    /** حذف فقط مشخصات فرم (خود عضو می‌ماند) */
    fun clearDetails(onDone: () -> Unit) {
        viewModelScope.launch {
            memberId?.let { detailsRepository.deleteByMember(it) }
            existingDetailsId = null
            details = MemberDetailsForm()
            onDone()
        }
    }

    fun archive(onDone: () -> Unit) {
        viewModelScope.launch {
            memberId?.let { memberRepository.setArchived(it, true) }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            memberId?.let { id ->
                memberRepository.getById(id)?.let { memberRepository.delete(it) }
            }
            onDone()
        }
    }

    companion object {
        fun factory(memberId: String?, folderId: String? = null): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YadyarApp
                MemberFormViewModel(
                    memberId,
                    folderId,
                    app.container.memberRepository,
                    app.container.memberDetailsRepository
                )
            }
        }
    }
}
