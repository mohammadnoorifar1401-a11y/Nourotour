package com.yadyar.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "member_details",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memberId"], unique = true)]
)
data class MemberDetails(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val memberId: String = "",

    // ---- مشخصات هویتی ----
    val fatherName: String = "",
    val nationalId: String = "",
    val nationalIdSerial: String = "",
    val birthDate: String = "",
    val birthPlace: String = "",
    val certNo: String = "",
    val certSerial: String = "",
    val postalCode: String = "",
    val landlinePhone: String = "",
    val sanaStatus: String = "", // دارد / ندارد

    // ---- تماس و سیم‌کارت ----
    val phone: String = "",
    val operatorName: String = "",
    val simPuk: String = "",

    // ---- اطلاعات بانکی و کارت ----
    val accountNo: String = "",
    val cardNo: String = "",
    val cardExp: String = "",
    val cardCvv2: String = "",
    val cardPassword: String = "",
    val username: String = "",
    val password: String = "",
    val digitalBranch: String = "", // شعبه افتتاح حساب دیجیتال
    val currentBranch: String = "", // شعبه افتتاح حساب جاری
    val digitalSignature: String = "", // دارد / ندارد
    val checkCertSymbol: String = "", // دارد / ندارد
    val tokenStatus: String = "", // فعال است / فعال نیست
    val creditReportUri: String = "", // محل بارگذاری گزارش اعتباری

    // ---- وضعیت اعتباری و تسهیلات ----
    val creditScore: String = "",
    val checkStatus: String = "",
    val errors: String = "",
    val freeCapacity: String = "",
    val activeLoans: String = "",
    val takenLoans: String = "",
    val collateral: String = "",
    val balanceStatus: String = "", // مانده / تخلیه

    // ---- مدارک (URI عکس‌ها) ----
    val shenasnamehPhotoUri: String = "",
    val nationalCardPhotoUri: String = "",
    val employmentCertPhotoUri: String = "",
    val payslipPhotoUri: String = "",
    val extraDocsUris: String = "", // لیست URI ها با | جدا شده

    val updatedAt: Long = System.currentTimeMillis()
)
