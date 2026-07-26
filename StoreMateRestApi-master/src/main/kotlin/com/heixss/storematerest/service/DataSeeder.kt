package com.heixss.storematerest.service

import com.heixss.storematerest.model.Product
import com.heixss.storematerest.model.Store
import com.heixss.storematerest.model.Supplier
import com.heixss.storematerest.model.Transaction
import com.heixss.storematerest.model.TransactionType
import com.heixss.storematerest.model.User
import com.heixss.storematerest.repository.ProductRepository
import com.heixss.storematerest.repository.SupplierRepository
import com.heixss.storematerest.repository.TransactionRepository
import com.heixss.storematerest.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Seeds a demo/demo account on a brand-new database so the Android app has
 * something to talk to out of the box.
 *
 * Disabled with `storemate.seed-demo-data=false` — a real shop deployment must
 * not ship with a well-known password.
 */
@Component
@ConditionalOnProperty(
    prefix = "storemate",
    name = ["seed-demo-data"],
    havingValue = "true",
    matchIfMissing = false
)
class DataSeeder(
    private val userRepo: UserRepository,
    private val supplierRepo: SupplierRepository,
    private val productRepo: ProductRepository,
    private val transactionRepo: TransactionRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(DataSeeder::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        if (userRepo.count() > 0) return

        val demoUser = userRepo.save(
            User(
                username = "demo",
                password = passwordEncoder.encode("demo"),
                store = Store(name = "Demo Store")
            )
        )
        val store = demoUser.store

        val supplier1 = supplierRepo.save(
            Supplier(
                name = "Supplier Ion",
                contactPerson = "Ion Popescu",
                phone = "0123456789",
                email = "ion@example.com",
                address = "Strada Mare 1",
                store = store
            )
        )
        val supplier2 = supplierRepo.save(
            Supplier(
                name = "Supplier Maria",
                contactPerson = "Maria Ionescu",
                phone = "0987654321",
                email = "maria@example.com",
                address = "Strada Lunga 5",
                store = store
            )
        )

        val product1 = productRepo.save(
            Product(
                name = "Cârnați de casă",
                description = "Cârnați tradiționali",
                price = 25.5,
                category = "Alimente",
                barcode = "1234567890123",
                supplier = supplier1,
                currentStockLevel = 10,
                minimumStockLevel = 2,
                store = store
            )
        )
        val product2 = productRepo.save(
            Product(
                name = "Brânză de burduf",
                description = "Brânză sărată",
                price = 18.0,
                category = "Alimente",
                barcode = "9876543210987",
                supplier = supplier2,
                currentStockLevel = 20,
                minimumStockLevel = 5,
                store = store
            )
        )

        // Stock levels above already reflect these opening restocks, so the
        // seeded history and the seeded stock agree.
        listOf(product1 to 10, product2 to 20).forEach { (product, quantity) ->
            transactionRepo.save(
                Transaction(
                    date = LocalDateTime.now(),
                    type = TransactionType.RESTOCK,
                    product = product,
                    quantity = quantity,
                    notes = "Initial restock",
                    store = store
                )
            )
        }

        log.info("Seeded demo account (demo/demo) with sample suppliers, products and transactions")
    }
}
