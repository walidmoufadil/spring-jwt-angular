# Application de Gestion Bancaire
### _Système sécurisé avec Spring Boot et JWT_

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![JWT](https://img.shields.io/badge/JWT-Authentication-blue)
![JPA](https://img.shields.io/badge/JPA-Persistence-orange)
![Status](https://img.shields.io/badge/Status-Production-green)

## Table des matières

1. [Présentation du projet](#1-présentation-du-projet)
2. [Architecture technique](#2-architecture-technique)
3. [Couches de l'application](#3-couches-de-lapplication)
   - [3.1 Couche Entités](#31-couche-entités)
   - [3.2 Couche Repositories](#32-couche-repositories)
   - [3.3 Couche DTO](#33-couche-dto-data-transfer-objects)
   - [3.4 Couche Mappers](#34-couche-mappers)
   - [3.5 Couche Services](#35-couche-services)
   - [3.6 Couche Exceptions](#36-couche-exceptions)
   - [3.7 Couche Contrôleurs](#37-couche-contrôleurs-web)
4. [Sécurité de l'application](#4-sécurité-de-lapplication)
5. [Guide de déploiement](#5-guide-de-déploiement)
6. [API Reference](#6-api-reference)
7. [Technologies utilisées](#7-technologies-utilisées)

## 1. Présentation du projet

Cette application de gestion bancaire est un système complet développé avec **Spring Boot**, offrant une solution robuste pour la gestion des comptes bancaires, des clients et des opérations financières. Le système implémente une sécurité avancée basée sur **JWT** (JSON Web Token) pour l'authentification et l'autorisation des utilisateurs.

**Fonctionnalités principales :**
- Gestion des clients (création, consultation, modification, suppression)
- Gestion des comptes bancaires (courants et d'épargne)
- Opérations financières sécurisées (dépôt, retrait, transfert)
- Historique des transactions avec pagination
- Système d'authentification et d'autorisation basé sur les rôles
- API REST complète

## 2. Architecture technique

L'application suit une **architecture en couches** bien définie qui permet une séparation claire des responsabilités et une maintenance facilitée :

![Architecture en couches](https://via.placeholder.com/800x400?text=Architecture+en+couches)

```
+-----------------------------------+
|           Couche Web              |
|       (Contrôleurs REST)          |
+-----------------------------------+
                 |
+-----------------------------------+
|           Couche DTO              |
|    (Objets de transfert de données) 
+-----------------------------------+
                 |
+-----------------------------------+
|          Couche Service           |
|      (Logique métier)             |
+-----------------------------------+
                 |
+-----------------------------------+
|       Couche Repository           |
|      (Accès aux données)          |
+-----------------------------------+
                 |
+-----------------------------------+
|         Couche Entités            |
|       (Modèle de données)         |
+-----------------------------------+
```

## 3. Couches de l'application

### 3.1 Couche Entités

Cette couche définit le modèle de données de l'application à travers des entités JPA.

#### Entités bancaires

| Entité | Description | Attributs principaux | Relations |
|--------|-------------|----------------------|-----------|
| **BankAccount** | Classe abstraite pour tous les comptes | id, balance, createdAt, status | ManyToOne avec Customer, OneToMany avec AccountOperation |
| **CurrentAccount** | Compte courant | overDraft | Hérite de BankAccount |
| **SavingAccount** | Compte épargne | interestRate | Hérite de BankAccount |
| **AccountOperation** | Opération financière | id, date, montant, type, description | ManyToOne avec BankAccount |

#### Entités utilisateur

| Entité | Description | Attributs principaux | Relations |
|--------|-------------|----------------------|-----------|
| **Customer** | Client de la banque | id, name, email | OneToOne avec Account, OneToMany avec BankAccount |
| **Account** | Compte utilisateur pour authentification | id, username, password | ManyToMany avec Role, OneToOne avec Customer |
| **Role** | Rôle pour les autorisations | id, roleName, description | ManyToMany avec Account |

#### Énumérations
- **AccountStatus** : CREATED, ACTIVATED, SUSPENDED
- **OperationType** : DEBIT, CREDIT

### 3.2 Couche Repositories

La couche repository s'appuie sur Spring Data JPA pour l'accès aux données. Chaque interface étend `JpaRepository` pour bénéficier des fonctionnalités CRUD de base.

#### AccountRepository
```java
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}
```

#### CustomerRepository
```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("select c from Customer c where c.name like :kw")
    List<Customer> searchCustomer(@Param("kw") String keyword);
    Optional<Customer> findByEmail(String email);
}
```

#### RoleRepository
```java
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);
    
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE :kw")
    Role searchRoleByName(@Param("kw") String keyword);
}
```

#### BankAccountRepository
```java
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
}
```

#### AccountOperationRepository
```java
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    List<AccountOperation> findByBankAccountId(String accountId);
    Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String accountId, Pageable pageable);
}
```

### 3.3 Couche DTO (Data Transfer Objects)

Cette couche définit les objets utilisés pour le transfert de données entre le client et le serveur, établissant une séparation claire entre les entités JPA et l'API.

#### DTOs principaux

| DTO | Description | Attributs clés |
|-----|-------------|----------------|
| **CustomerDTO** | Représentation d'un client | id, name, email, password |
| **BankAccountDTO** | Classe abstraite pour les comptes | type |
| **CurrentBankAccountDTO** | Représentation d'un compte courant | id, balance, status, overDraft |
| **SavingBankAccountDTO** | Représentation d'un compte épargne | id, balance, status, interestRate |

#### DTOs pour les opérations

| DTO | Description | Attributs clés |
|-----|-------------|----------------|
| **AccountOperationDTO** | Représentation d'une opération | id, date, montant, type, description |
| **AccountHistoryDTO** | Historique paginé des opérations | accountId, balance, currentPage, totalPages, operations |
| **DebitDTO/CreditDTO** | Requêtes de débit/crédit | accountId, amount, description |
| **TransferRequestDTO** | Requête de transfert | sourceAccount, destinationAccount, amount |
| **ChangePasswordDTO** | Changement de mot de passe | oldPassword, newPassword |

### 3.4 Couche Mappers

Les mappers assurent la conversion bidirectionnelle entre les entités et les DTOs, facilitant ainsi la séparation entre la couche métier et la couche présentation.

```java
@Service
public class BankAccountMapperImpl {
    // Méthodes de conversion Customer ↔ CustomerDTO
    public CustomerDTO fromCustomer(Customer customer) { ... }
    public Customer fromCustomerDTO(CustomerDTO customerDTO) { ... }
    
    // Méthodes de conversion pour les autres entités...
}
```

**Exemple d'implémentation :**

```java
public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount) {
    SavingBankAccountDTO dto = new SavingBankAccountDTO();
    BeanUtils.copyProperties(savingAccount, dto);
    dto.setCustomerDTO(fromCustomer(savingAccount.getCustomer()));
    dto.setType(savingAccount.getClass().getSimpleName());
    return dto;
}
```

### 3.5 Couche Services

La couche service implémente la logique métier de l'application, orchestrant les opérations entre les repositories et assurant la transformation des données via les mappers.

#### Services principaux

**BankAccountService** - Interface définissant les opérations bancaires :
```java
public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId);
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId);
    void debit(String accountId, double amount, String description);
    void credit(String accountId, double amount, String description);
    void transfer(String accountIdSource, String accountIdDestination, double amount);
    // Autres méthodes...
}
```

**AccountService** - Interface pour la gestion des comptes utilisateurs :
```java
public interface AccountService {
    Account addNewAccount(String username, String password, String roleName);
    void addRoleToAccount(String username, String roleName);
    Role addNewRole(String roleName, String description);
    void changePassword(String username, String oldPassword, String newPassword);
    // Autres méthodes...
}
```

#### Intégration avec Spring Security

```java
@Service
@AllArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

### 3.6 Couche Exceptions

Cette couche gère les erreurs métier de manière structurée à travers des exceptions personnalisées.

#### Exceptions principales

```java
public class CustomerNotFoundException extends Exception {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}

public class BankAccountNotFoundException extends Exception {
    public BankAccountNotFoundException(String message) {
        super(message);
    }
}

public class BalanceNotSufficientException extends Exception {
    public BalanceNotSufficientException(String message) {
        super(message);
    }
}
```

#### Gestionnaire global d'exceptions

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
    
    // Autres méthodes de gestion d'exceptions...
}
```

### 3.7 Couche Contrôleurs (Web)

Cette couche expose les fonctionnalités de l'application via des API REST et gère l'interaction avec les clients HTTP.

#### SecurityController
```java
@RestController
@AllArgsConstructor
@Slf4j
public class SecurityController {
    // Services injectés...

    @GetMapping("/profile")
    public Authentication infos(Authentication authentication){
        return authentication;
    }

    @PostMapping("/auth")
    public Map<String,String> token(@RequestParam String username, @RequestParam String password){
        // Génération du token JWT...
    }

    @PostMapping("/change-password")
    public void changePassword(Authentication authentication, @RequestBody ChangePasswordDTO dto) {
        // Changement de mot de passe...
    }
}
```

#### CustomerRestController
```java
@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CustomerRestController {
    // Services injectés...

    @GetMapping("/customers")
    public List<CustomerDTO> customers(){
        return bankAccountService.listCustomers();
    }

    // Autres endpoints...
}
```

#### BankAccountRestAPI
```java
@RestController
@CrossOrigin("*")
public class BankAccountRestAPI {
    // Services injectés...

    @PostMapping("/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.debit(debitDTO.getAccountId(), debitDTO.getAmount(), debitDTO.getDescription());
        return debitDTO;
    }

    // Autres endpoints...
}
```

## 4. Sécurité de l'application

La sécurité est un aspect fondamental de cette application bancaire. Elle repose sur **Spring Security** et **JWT** pour assurer l'authentification et l'autorisation des utilisateurs.

### 4.1 Configuration de sécurité

La classe `SecurityConfig` orchestre tous les aspects de sécurité de l'application :

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${secret.key}")
    private String secretKey;

    private final CustomerUserDetailsService customerUserDetailsService;

    // Constructeur avec injection du service d'authentification
    public SecurityConfig(CustomerUserDetailsService customerUserDetailsService) {
        this.customerUserDetailsService = customerUserDetailsService;
    }

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(customerUserDetailsService);
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf-> csrf.disable())
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(ar->ar.requestMatchers("/auth/**","/swagger-ui/**","/v3/api-docs/**").permitAll())
                .authorizeHttpRequests(ar->ar.anyRequest().authenticated())
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2->oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
    
    // Autres méthodes de configuration...
}
```

### 4.2 Implémentation JWT

L'application utilise **JSON Web Token (JWT)** pour l'authentification sans état (stateless), ce qui permet une meilleure scalabilité et évite de stocker des informations de session sur le serveur.

#### Encodeur et décodeur JWT

```java
@Bean
JwtEncoder jwtEncoder(){
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getBytes()));
}

@Bean
JwtDecoder jwtDecoder(){
    SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "RSA");
    return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                           .macAlgorithm(MacAlgorithm.HS512)
                           .build();
}
```

La clé secrète est chargée depuis le fichier de propriétés de l'application (`application.properties`) :

```properties
secret.key=9faa372517ac1d389758d3750fc07acf00f542277f26fec1ce4593e93f64e338
```

### 4.3 Gestion des utilisateurs

La gestion des utilisateurs repose sur les entités `Account` et `Role` qui implémentent l'interface `UserDetails` de Spring Security, permettant ainsi une intégration complète avec le système d'authentification.

#### Service d'authentification personnalisé

```java
@Service
@AllArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

### 4.4 Sécurité au niveau des méthodes

L'annotation `@EnableMethodSecurity` active la sécurité au niveau des méthodes, permettant l'utilisation des annotations `@PreAuthorize` pour contrôler l'accès basé sur les rôles :

```java
@GetMapping("/customers/{id}")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
public CustomerDTO getCustomer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
    return bankAccountService.getCustomer(customerId);
}
```

### 4.5 Configuration CORS

La configuration CORS (Cross-Origin Resource Sharing) permet de contrôler précisément quelles origines peuvent accéder à l'API REST :

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

Cette configuration est particulièrement importante pour permettre aux applications frontend (comme Angular fonctionnant sur le port 4200) d'interagir avec l'API.

### 4.6 Cycle de vie d'authentification

Le processus d'authentification JWT suit ces étapes :

1. **Demande d'authentification** : L'utilisateur envoie ses credentials (username/password) au endpoint `/auth`
2. **Vérification** : Le `AuthenticationManager` vérifie les credentials via le `CustomerUserDetailsService`
3. **Génération du token** : Si les credentials sont valides, un JWT est généré et retourné au client
4. **Stockage client** : Le client stocke le token (généralement dans localStorage ou sessionStorage)
5. **Requêtes authentifiées** : Le client inclut le token dans l'en-tête `Authorization` pour toutes les requêtes ultérieures
6. **Validation des requêtes** : Le serveur valide le token JWT pour chaque requête protégée

### 4.7 Chiffrement des mots de passe

L'application utilise BCrypt pour le hachage sécurisé des mots de passe :

```java
@Bean
PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
}
```

BCrypt est un algorithme de hachage adaptatif qui intègre automatiquement un salt et est résistant aux attaques par force brute grâce à son facteur de coût paramétrable.

## 5. Guide de déploiement

### Prérequis
- Java 17+
- Maven 3.6+
- Base de données (MySQL, PostgreSQL, H2)

### Étapes de déploiement

1. **Cloner le repository**
   ```bash
   git clone https://github.com/username/spring-jwt.git
   cd spring-jwt
   ```

2. **Configurer la base de données**
   - Éditer `application.properties` avec vos paramètres de connexion

3. **Compiler l'application**
   ```bash
   mvn clean install
   ```

4. **Exécuter l'application**
   ```bash
   mvn spring-boot:run
   ```
   ou
   ```bash
   java -jar target/spring-jwt-0.0.1-SNAPSHOT.jar
   ```

5. **Accéder à l'application**
   - L'API REST est disponible à l'adresse : `http://localhost:8080`

## 6. API Reference

### Authentification
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/auth` | Authentification et génération de token JWT |
| GET | `/profile` | Récupérer le profil utilisateur connecté |
| POST | `/change-password` | Changer le mot de passe utilisateur |

### Gestion des clients
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/customers` | Lister tous les clients |
| GET | `/customers/{id}` | Récupérer un client par ID |
| POST | `/customers` | Créer un nouveau client |
| PUT | `/customers/{id}` | Mettre à jour un client |
| DELETE | `/customers/{id}` | Supprimer un client |
| GET | `/customers/search?keyword=` | Rechercher des clients |

### Gestion des comptes bancaires
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/accounts` | Lister tous les comptes bancaires |
| GET | `/accounts/{id}` | Récupérer un compte par ID |
| POST | `/accounts` | Créer un nouveau compte |
| GET | `/accounts/{id}/operations` | Voir l'historique des opérations |
| GET | `/accounts/{id}/operations/page?page=0&size=5` | Historique paginé |

### Opérations bancaires
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/accounts/debit` | Effectuer un débit |
| POST | `/accounts/credit` | Effectuer un crédit |
| POST | `/accounts/transfer` | Effectuer un transfert |

## 7. Technologies utilisées

- ![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white) **Spring Boot** - Framework Java pour applications d'entreprise
- ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=spring-security&logoColor=white) **Spring Security** - Authentification et autorisation
- ![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=json-web-tokens&logoColor=white) **JSON Web Token** - Sécurité stateless
- ![JPA](https://img.shields.io/badge/JPA-527FFF?style=flat&logo=java&logoColor=white) **Spring Data JPA** - Accès aux données
- ![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat&logo=apache-maven&logoColor=white) **Maven** - Gestion des dépendances
- ![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=flat&logo=java&logoColor=white) **Lombok** - Réduction du code boilerplate
- ![Database](https://img.shields.io/badge/Database-4479A1?style=flat&logo=mysql&logoColor=white) **Base de données** - Stockage persistant (configurable)
