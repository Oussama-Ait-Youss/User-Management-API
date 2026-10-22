# Documentation Spring — Core, Data JPA et MVC (sans Spring Boot)

Ce document répond, question par question, aux notions essentielles du **Spring Framework** utilisé **sans Spring Boot** : l'inversion de contrôle, la persistance avec JPA et la couche web avec Spring MVC.

> **Remarque :** les exemples utilisent Spring Framework 6+ avec l'espace de noms `jakarta.*` (et non `javax.*`). Le serveur doit donc être compatible Jakarta EE 9+ (ex. Tomcat 10.1+).

## Sommaire

1. [Spring Core — Inversion de Contrôle & Injection de Dépendances](#spring-core) (questions 1 à 17)
2. [Spring Data JPA — Persistance & Transactions](#spring-data-jpa) (questions 18 à 31)
3. [Spring MVC — Contrôleurs & Couche Web](#spring-mvc) (questions 32 à 47)
---

<a id="spring-core"></a>
# Partie 1 — Spring Core : Inversion de Contrôle & Injection de Dépendances

### 1. Qu'est-ce que Spring Core et à quoi sert-il dans une application Java ?

**Spring Core** est le module de base du framework Spring. Il fournit le **conteneur IoC** : un "chef d'orchestre" qui crée les objets de l'application, les configure et les relie entre eux.

Il sert à :
- réduire le couplage entre les classes ;
- rendre le code plus simple à tester et à maintenir ;
- centraliser la configuration de l'application.
---

### 2. Que signifie le principe d'Inversion de Contrôle (IoC) ?

L'**Inversion de Contrôle** consiste à **confier au framework la création et la gestion des objets**, au lieu de les créer soi-même avec `new`. Le contrôle du flux est "inversé" : ce n'est plus ton code qui construit ses dépendances, c'est le conteneur qui les fournit.

```java
// Sans IoC : la classe crée elle-même sa dépendance
public class UserService {
    private UserRepository repo = new UserRepository();
}
 
// Avec IoC : la dépendance est fournie de l'extérieur
public class UserService {
    private final UserRepository repo;
 
    public UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```
 
---

### 3. Quelle est la différence entre IoC et Injection de Dépendances (DI) ?

- **IoC** est le **principe général** : "ne crée pas tes objets toi-même, laisse le conteneur le faire".
- **DI** (Dependency Injection) est la **technique concrète** qui met en œuvre ce principe : le conteneur "injecte" les dépendances dans les objets.
  Il existe trois types d'injection :

| Type | Exemple | Commentaire |
|---|---|---|
| Par constructeur | `public UserService(UserRepository repo)` | **Recommandée** (dépendances obligatoires, objets immuables) |
| Par setter | `setRepo(UserRepository repo)` | Pour les dépendances optionnelles |
| Par champ | `@Autowired private UserRepository repo;` | Pratique mais déconseillée (difficile à tester) |
 
---

### 4. Qu'est-ce qu'un bean dans Spring ?

Un **bean** est un **objet Java créé, configuré et géré par le conteneur Spring**. Tout objet qui n'est pas géré par Spring (créé avec `new`) n'est pas un bean.
 
---

### 5. Quel est le rôle du conteneur IoC ?

Le **conteneur IoC** est le cœur de Spring. Il :
1. lit la configuration (XML, annotations ou Java Config) ;
2. crée les beans ;
3. injecte les dépendances entre eux ;
4. gère leur cycle de vie (initialisation, destruction) ;
5. les met à disposition de l'application.
---

### 6. Quelle est la différence entre ApplicationContext et BeanFactory ?

Les deux sont des conteneurs IoC, mais `ApplicationContext` est une version plus complète.

| Critère | `BeanFactory` | `ApplicationContext` |
|---|---|---|
| Rôle | Conteneur de base | Conteneur avancé (étend `BeanFactory`) |
| Création des beans singleton | À la demande (*lazy*) | Au démarrage (*eager*) |
| Événements, internationalisation (i18n) | Non | Oui |
| Chargement de ressources, profils, `Environment` | Limité | Oui |
| Utilisation courante | Rare | **Standard** |

En pratique, on utilise **toujours `ApplicationContext`** (ex. `AnnotationConfigApplicationContext`).
 
---

### 7. Quelles sont les trois approches de configuration dans Spring (XML, annotations, Java Config) ?

**1. XML** — configuration externe, historique :
```xml
<bean id="userRepository" class="com.example.repository.UserRepository"/>
<bean id="userService" class="com.example.service.UserService">
    <constructor-arg ref="userRepository"/>
</bean>
```

**2. Annotations** — on annote les classes, Spring les détecte par scan :
```java
@Service
public class UserService { /* ... */ }
```

**3. Java Config** — on déclare les beans dans une classe Java :
```java
@Configuration
public class AppConfig {
    @Bean
    public UserService userService(UserRepository repo) {
        return new UserService(repo);
    }
}
```

Ces approches peuvent être **combinées**. Aujourd'hui, on privilégie **Java Config + annotations**.

Démarrage du conteneur :
```java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
UserService service = ctx.getBean(UserService.class);
```
 
---

### 8. À quoi servent les annotations suivantes ?

Voir les questions 9 à 13 ci-dessous.

### 9. `@Configuration`

Marque une classe comme **source de définitions de beans**. Elle remplace le fichier XML de configuration. Spring lit cette classe pour savoir quels beans créer.

```java
@Configuration
public class AppConfig { /* méthodes @Bean */ }
```
 
---

### 10. `@ComponentScan`

Indique à Spring **quels packages scanner** pour trouver automatiquement les classes annotées (`@Component`, `@Service`, `@Repository`, `@Controller`).

```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig { }
```
 
---

### 11. `@Bean`

Placée sur une **méthode** d'une classe `@Configuration`, elle indique que la valeur retournée est **un bean géré par Spring**. Le nom du bean est par défaut celui de la méthode. Très utile pour déclarer des classes **qu'on ne peut pas annoter** (classes de bibliothèques externes, `DataSource`, etc.).

```java
@Bean
public DataSource dataSource() {
    // création et configuration de la DataSource
}
```
 
---

### 12. `@Component`, `@Service`, `@Repository`, `@Controller`

Ce sont des **annotations "stéréotypes"** : elles marquent une classe comme bean détectable par le scan.

| Annotation | Couche | Rôle |
|---|---|---|
| `@Component` | Générique | Composant Spring quelconque |
| `@Service` | Métier | Contient la logique métier |
| `@Repository` | Accès aux données | Accède à la base ; traduit les exceptions techniques (ex. `SQLException`) en `DataAccessException` de Spring |
| `@Controller` | Web | Reçoit les requêtes HTTP (Spring MVC) |

`@Service`, `@Repository` et `@Controller` sont des spécialisations de `@Component` : elles ajoutent surtout de la **clarté** sur le rôle de la classe.
 
---

### 13. `@Autowired`, `@Qualifier`

- **`@Autowired`** : demande à Spring d'**injecter automatiquement** une dépendance, en cherchant un bean du **bon type**. Sur un constructeur unique, elle est facultative depuis Spring 4.3.
- **`@Qualifier("nom")`** : sert à **choisir un bean précis par son nom** quand plusieurs beans du même type existent (sinon Spring lève une erreur d'ambiguïté).
```java
@Service
public class NotificationService {
    private final MessageSender sender;
 
    public NotificationService(@Qualifier("emailSender") MessageSender sender) {
        this.sender = sender;
    }
}
```

> `@Primary` permet aussi de désigner un bean "par défaut" quand il y en a plusieurs.
 
---

### 14. Comment Spring détecte et crée automatiquement les composants ?

1. `@ComponentScan` indique les packages à explorer.
2. Spring parcourt le **classpath** et repère les classes annotées (`@Component`, `@Service`, etc.).
3. Pour chaque classe, il crée une **définition de bean** (`BeanDefinition`) et l'enregistre dans le conteneur.
4. Il **instancie** les beans (les singletons au démarrage).
5. Il **injecte** les dépendances (`@Autowired`, constructeur, etc.).
---

### 15. Quelles sont les étapes du cycle de vie d'un bean ?

1. **Instanciation** du bean (appel du constructeur).
2. **Injection** des dépendances.
3. Callbacks `Aware` (ex. `BeanNameAware`, `ApplicationContextAware`).
4. `BeanPostProcessor` — avant l'initialisation.
5. **Initialisation** : méthode `@PostConstruct`, `afterPropertiesSet()` ou `init-method`.
6. `BeanPostProcessor` — après l'initialisation.
7. **Bean prêt à être utilisé.**
8. **Destruction** à la fermeture du contexte : méthode `@PreDestroy`, `destroy()` ou `destroy-method`.
```java
@Component
public class CacheManager {
 
    @PostConstruct
    public void init() { System.out.println("Bean initialisé"); }
 
    @PreDestroy
    public void cleanup() { System.out.println("Bean détruit"); }
}
```

> Les beans de scope `prototype` ne sont **pas** détruits par Spring : seule leur création est gérée.
 
---

### 16. Quelle est la différence entre les scopes de bean (singleton, prototype, etc.) ?

Le **scope** définit **combien d'instances** d'un bean Spring crée et **combien de temps** elles vivent.

| Scope | Description |
|---|---|
| `singleton` (**défaut**) | Une seule instance partagée dans tout le conteneur |
| `prototype` | Une **nouvelle instance** à chaque demande |
| `request` | Une instance par requête HTTP (web) |
| `session` | Une instance par session HTTP (web) |
| `application` | Une instance par `ServletContext` (web) |
| `websocket` | Une instance par session WebSocket |

```java
@Component
@Scope("prototype")
public class ShoppingCart { }
```
 
---

### 17. Pourquoi la configuration manuelle (avant Spring Boot) est-elle importante à comprendre ?

Parce que **Spring Boot ne fait que configurer automatiquement ce qu'on écrirait à la main**. Comprendre la configuration manuelle permet de :
- savoir **ce qui se passe réellement** derrière l'auto-configuration ;
- **déboguer** plus facilement les erreurs (beans manquants, conflits, transactions qui ne fonctionnent pas) ;
- **personnaliser** la configuration quand l'auto-configuration ne suffit pas ;
- travailler sur des **projets existants (legacy)** qui n'utilisent pas Spring Boot.
---

<a id="spring-data-jpa"></a>
# Partie 2 — Spring Data JPA : Persistance & Transactions

### 18. Qu'est-ce que Spring Data JPA et quel problème résout-il ?

**Spring Data JPA** est un module qui **simplifie l'accès aux données** avec JPA. Il évite d'écrire à la main le code répétitif des DAO (CRUD, pagination, tri) : il suffit de **déclarer une interface**, et Spring génère l'implémentation.

Problème résolu : beaucoup de **code répétitif** (*boilerplate*) pour les opérations de base sur la base de données.
 
---

### 19. Quelle est la différence entre JPA et Hibernate ?

- **JPA** (Jakarta Persistence API) est une **spécification** : un ensemble d'interfaces et d'annotations qui définit *comment* mapper des objets Java vers des tables.
- **Hibernate** est une **implémentation** de cette spécification (la plus utilisée). Il fait réellement le travail (génération du SQL, gestion du cache, etc.).
> Analogie : JPA est le **plan**, Hibernate est la **maison construite** selon ce plan. D'autres implémentations existent (EclipseLink, par exemple).
 
---

### 20. Qu'est-ce qu'une entité JPA ?

Une **entité** est une **classe Java mappée à une table** de la base de données. Chaque instance correspond à une **ligne** de la table.

```java
@Entity
@Table(name = "users")
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 50)
    private String name;
 
    // constructeur sans argument + getters/setters
}
```
 
---

### 21. À quoi sert le DataSource ?

Le **`DataSource`** est l'objet qui **fournit les connexions** à la base de données (URL, utilisateur, mot de passe, driver). Il gère généralement un **pool de connexions** (ex. HikariCP) pour éviter d'ouvrir une nouvelle connexion à chaque requête.
 
---

### 22. Que fait l'EntityManager ?

L'**`EntityManager`** est l'interface JPA principale pour **dialoguer avec la base de données** : `persist()`, `find()`, `merge()`, `remove()`, `createQuery()`. Il gère aussi le **contexte de persistance** : l'ensemble des entités "suivies", dont les modifications sont synchronisées automatiquement avec la base.
 
---

### 23. Quelle est la responsabilité du TransactionManager ?

Le **`TransactionManager`** (pour JPA : `JpaTransactionManager`) **démarre, valide (*commit*) ou annule (*rollback*)** les transactions. Il relie la transaction à l'`EntityManager` et à la connexion utilisée, afin que toutes les opérations d'une même transaction réussissent ensemble ou échouent ensemble.
 
---

### 24. À quoi sert l'annotation `@EnableJpaRepositories` ?

Elle **active Spring Data JPA** : Spring scanne les packages indiqués, repère les interfaces de repository et **génère leurs implémentations** (proxies).

```java
@Configuration
@EnableJpaRepositories(basePackages = "com.example.repository")
public class PersistenceConfig { }
```
 
---

### 25. Qu'est-ce qu'un repository Spring Data ?

Un **repository** est une **interface** qui étend `JpaRepository` (ou `CrudRepository`). Spring en fournit automatiquement l'implémentation. On peut y ajouter des **méthodes de requête dérivées du nom** ou des requêtes personnalisées avec `@Query`.

```java
public interface UserRepository extends JpaRepository<User, Long> {
 
    Optional<User> findByEmail(String email);
 
    List<User> findByNameContainingIgnoreCase(String name);
 
    @Query("SELECT u FROM User u WHERE u.age > :age")
    List<User> findOlderThan(@Param("age") int age);
}
```
 
---

### 26. Quelles sont les méthodes génériques fournies par JpaRepository ?

| Méthode | Description |
|---|---|
| `save(entity)` | Insère ou met à jour une entité |
| `saveAll(entities)` | Enregistre plusieurs entités |
| `findById(id)` | Recherche par identifiant (retourne un `Optional`) |
| `findAll()` | Retourne toutes les entités |
| `findAll(Pageable)` / `findAll(Sort)` | Pagination et tri |
| `existsById(id)` | Vérifie l'existence |
| `count()` | Nombre d'entités |
| `deleteById(id)` / `delete(entity)` | Supprime une entité |
| `deleteAll()` | Supprime tout |
| `flush()` / `saveAndFlush(entity)` | Force la synchronisation avec la base |
| `getReferenceById(id)` | Retourne une référence "paresseuse" (proxy) |
 
---

### 27. Comment gérer les transactions avec Spring (annotation, propagation, rollback, etc.) ?

**Activation :** `@EnableTransactionManagement` sur une classe de configuration, puis `@Transactional` sur les méthodes (ou classes) **de la couche service**.

```java
@Service
public class OrderService {
 
    @Transactional
    public void placeOrder(Order order) { /* ... */ }
 
    @Transactional(readOnly = true)
    public List<Order> findAll() { /* ... */ }
 
    @Transactional(rollbackFor = Exception.class)
    public void process() throws Exception { /* ... */ }
}
```

**Paramètres principaux de `@Transactional` :**

| Paramètre | Rôle |
|---|---|
| `propagation` | Comment se comporte la méthode si une transaction existe déjà |
| `isolation` | Niveau d'isolation entre transactions concurrentes |
| `readOnly` | Indique une transaction en lecture seule (optimisation) |
| `timeout` | Durée maximale avant annulation |
| `rollbackFor` / `noRollbackFor` | Exceptions qui déclenchent (ou non) un rollback |

**Propagations courantes :**
- `REQUIRED` (**défaut**) : rejoint la transaction existante ou en crée une nouvelle.
- `REQUIRES_NEW` : suspend l'existante et en crée toujours une nouvelle.
- `SUPPORTS` : utilise la transaction si elle existe, sinon s'exécute sans.
- `MANDATORY` : exige une transaction existante (sinon exception).
- `NOT_SUPPORTED` / `NEVER` : s'exécute sans transaction (ou interdit d'en avoir une).
- `NESTED` : transaction imbriquée (avec points de sauvegarde).
  **Rollback par défaut :** uniquement pour les `RuntimeException` et les `Error`. Les exceptions *checked* ne déclenchent **pas** de rollback, sauf avec `rollbackFor`.

**Pièges fréquents :**
- Un appel d'une méthode `@Transactional` **depuis la même classe** (*self-invocation*) ne passe pas par le proxy : la transaction est ignorée.
- `@Transactional` n'a pas d'effet sur une méthode `private`.
---

### 28. Pourquoi définir manuellement la connexion à la base de données avant Spring Boot ?

Parce que **sans Spring Boot, rien n'est configuré automatiquement**. Spring Boot crée la `DataSource` à partir de `application.properties`. Sans lui, c'est au développeur de déclarer lui-même les beans de connexion (`DataSource`, `EntityManagerFactory`, `TransactionManager`). C'est aussi ce qui donne un **contrôle total** sur la configuration.
 
---

### 29. Que doit contenir une configuration de persistance complète (DataSource, EntityManagerFactory, TransactionManager) ?

Une configuration complète contient **quatre éléments** :

1. une **`DataSource`** (connexion à la base) ;
2. une **`LocalContainerEntityManagerFactoryBean`** (fabrique des `EntityManager`, avec le package des entités et le fournisseur JPA, ex. Hibernate) ;
3. un **`JpaTransactionManager`** ;
4. les annotations **`@EnableTransactionManagement`** et **`@EnableJpaRepositories`**.
```java
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.repository")
@PropertySource("classpath:application.properties")
public class PersistenceConfig {
 
    private final Environment env;
 
    public PersistenceConfig(Environment env) {
        this.env = env;
    }
 
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(env.getProperty("db.driver"));
        ds.setJdbcUrl(env.getProperty("db.url"));
        ds.setUsername(env.getProperty("db.username"));
        ds.setPassword(env.getProperty("db.password"));
        return ds;
    }
 
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.model");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
 
        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", "update"); // à éviter en production
        props.setProperty("hibernate.show_sql", "true");
        emf.setJpaProperties(props);
        return emf;
    }
 
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

Exemple de fichier `application.properties` :
```properties
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/mabase
db.username=postgres
db.password=secret
```
 
---

### 30. Qu'est-ce que la validation de contrainte dans le modèle (ex. email unique, longueur, etc.) ?

C'est l'ensemble des **règles qui garantissent que les données sont valides** avant (et pendant) leur enregistrement. Il existe **deux niveaux** :

- **Bean Validation** (`jakarta.validation`, implémentée par Hibernate Validator) : contrôle les valeurs côté application (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`, `@Pattern`).
- **Contraintes de base de données** (via JPA) : `@Column(unique = true, nullable = false, length = 100)`. La base reste la **garantie finale**. L'unicité, par exemple, ne peut être vraiment assurée que par la base.
```java
@Entity
@Table(name = "users")
public class User {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String name;
 
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;
}
```

> Une violation d'unicité lève une `DataIntegrityViolationException` qu'il faut gérer (voir `@RestControllerAdvice`, question 46).
 
---

### 31. Quelle est la différence entre une suppression logique (soft delete) et une suppression physique ?

| Critère | Suppression **physique** (*hard delete*) | Suppression **logique** (*soft delete*) |
|---|---|---|
| Principe | La ligne est **effacée** de la table (`DELETE`) | La ligne est **marquée comme supprimée** (`deleted = true` ou `deleted_at`) |
| Récupération | Impossible | Possible |
| Historique / audit | Perdu | Conservé |
| Espace disque | Libéré | Les données restent |
| Complexité | Simple | Il faut filtrer les lignes supprimées dans les requêtes |

Exemple avec Hibernate :
```java
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User {
    // ...
    private boolean deleted = false;
}
```

> `@SQLRestriction` est disponible à partir de Hibernate 6.3 (elle remplace `@Where`). Les versions récentes proposent aussi `@SoftDelete`.
 
---

<a id="spring-mvc"></a>
# Partie 3 — Spring MVC : Contrôleurs & Couche Web

### 32. Que signifie MVC (Model-View-Controller) et quel est son objectif dans Spring ?

**MVC** est un **patron de conception** qui sépare l'application en trois parties :

- **Model** : les données et la logique métier ;
- **View** : l'affichage (page HTML, JSP, Thymeleaf, ou réponse JSON) ;
- **Controller** : reçoit les requêtes, appelle les services et choisit la réponse à renvoyer.
  **Objectif :** **séparer les responsabilités** pour obtenir un code plus clair, plus facile à tester et à faire évoluer.

---

### 33. Quel est le rôle du DispatcherServlet dans Spring MVC ?

Le **`DispatcherServlet`** est le **point d'entrée unique** de Spring MVC (patron *Front Controller*). Toutes les requêtes HTTP passent par lui. Il ne traite rien lui-même : il **délègue** aux bons composants (`HandlerMapping`, `HandlerAdapter`, `ViewResolver`, convertisseurs de messages, etc.).
 
---

### 34. Quelle est la différence entre un Controller et un RestController ?

| | `@Controller` | `@RestController` |
|---|---|---|
| Retour de méthode | Nom d'une **vue** (page HTML) | **Données** directement (JSON, XML) |
| Rendu | Via un `ViewResolver` | Écrit dans le corps de la réponse HTTP |
| Équivalence | — | `@Controller` + `@ResponseBody` |
| Usage | Applications web avec pages | **API REST** |
 
---

### 35. Quelle est la fonction des annotations suivantes ?

Voir les questions 36 à 39 ci-dessous.

### 36. `@RequestMapping`

**Associe une URL (et éventuellement une méthode HTTP) à une classe ou une méthode** du contrôleur. Attributs principaux : `value`/`path`, `method`, `consumes`, `produces`, `params`, `headers`.

```java
@RestController
@RequestMapping("/api/users")
public class UserController { }
```
 
---

### 37. `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`

Ce sont des **raccourcis de `@RequestMapping`** pour chaque méthode HTTP :

| Annotation | Méthode HTTP | Usage typique |
|---|---|---|
| `@GetMapping` | GET | **Lire** une ressource |
| `@PostMapping` | POST | **Créer** une ressource |
| `@PutMapping` | PUT | **Modifier** (remplacer) une ressource |
| `@DeleteMapping` | DELETE | **Supprimer** une ressource |

(`@PatchMapping` existe aussi pour les modifications partielles.)
 
---

### 38. `@Valid`

Déclenche la **validation Bean Validation** sur un objet (par exemple le corps de la requête). Si une contrainte n'est pas respectée (`@NotBlank`, `@Email`, etc.), Spring lève une `MethodArgumentNotValidException` (ou remplit un `BindingResult` si on le déclare en paramètre).

```java
@PostMapping
public UserDto create(@Valid @RequestBody UserDto dto) { /* ... */ }
```
 
---

### 39. `@RequestBody` et `@PathVariable`

- **`@RequestBody`** : transforme le **corps JSON de la requête** en objet Java (désérialisation).
- **`@PathVariable`** : récupère une **valeur dans l'URL** (ex. l'identifiant).
```java
@PutMapping("/{id}")
public UserDto update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
    return userService.update(id, dto);
}
```

> `@RequestParam` permet quant à lui de lire un paramètre de requête (ex. `/users?page=2`).
 
---

### 40. Comment le DispatcherServlet traite-t-il une requête HTTP du début à la fin ?

```
Client HTTP
    │
    ▼
DispatcherServlet ──► HandlerMapping      (quel contrôleur / quelle méthode ?)
    │
    ├──► Interceptors (preHandle)
    │
    ├──► HandlerAdapter ──► Contrôleur     (conversion des arguments, validation)
    │                          │
    │                          ▼
    │                       Service ──► Repository ──► Base de données
    │
    ├──► Interceptors (postHandle)
    │
    ├──► ViewResolver (page HTML)  ou  HttpMessageConverter (JSON)
    │
    ▼
Réponse HTTP renvoyée au client
```

Étapes :
1. Le `DispatcherServlet` **reçoit** la requête.
2. Le **`HandlerMapping`** trouve le contrôleur et la méthode qui correspondent à l'URL et à la méthode HTTP.
3. Les **intercepteurs** (`preHandle`) s'exécutent.
4. Le **`HandlerAdapter`** appelle la méthode du contrôleur (en convertissant les arguments : `@PathVariable`, `@RequestBody`, `@Valid`...).
5. Le contrôleur retourne un résultat (nom de vue, objet, `ResponseEntity`).
6. Selon le cas : le **`ViewResolver`** produit une vue HTML, ou un **`HttpMessageConverter`** sérialise l'objet en JSON.
7. En cas d'exception, un **`HandlerExceptionResolver`** (ex. `@RestControllerAdvice`) prend le relais.
8. La **réponse HTTP** est envoyée au client.
---

### 41. Qu'est-ce que la classe de configuration Web (WebConfig) et à quoi sert-elle ?

**`WebConfig`** est une classe de configuration Java qui définit **le comportement de Spring MVC** : où sont les contrôleurs, quels convertisseurs JSON utiliser, les intercepteurs, le CORS, les ressources statiques, les résolveurs de vues, etc.

```java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.example.controller")
public class WebConfig implements WebMvcConfigurer {
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("http://localhost:4200");
    }
}
```

- `@EnableWebMvc` : active la configuration Spring MVC (dont les convertisseurs JSON si Jackson est présent).
- `WebMvcConfigurer` : interface qui permet de **personnaliser** cette configuration.
---

### 42. Pourquoi faut-il initialiser le DispatcherServlet manuellement avant Spring Boot ?

Parce que le **serveur d'applications (Tomcat, etc.) ne connaît pas Spring**. Il faut donc lui dire d'**enregistrer le `DispatcherServlet`** et de lui associer le contexte Spring. Avec Spring Boot, c'est fait automatiquement (serveur embarqué et auto-configuration) ; sans Boot, c'est au développeur de le déclarer.
 
---

### 43. Qu'est-ce qu'un WebAppInitializer et pourquoi remplace-t-il web.xml ?

Un **`WebAppInitializer`** est une classe Java qui **configure l'application web au démarrage du serveur** : elle enregistre le `DispatcherServlet` et crée les contextes Spring. Il remplace le fichier **`web.xml`** (configuration XML historique) par du **code Java**, plus lisible et vérifiable à la compilation.

Le serveur (Servlet 3.0+) détecte automatiquement cette classe grâce à `SpringServletContainerInitializer`.

```java
public class WebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {
 
    @Override
    protected Class<?>[] getRootConfigClasses() {
        // contexte racine : services, repositories, persistance
        return new Class<?>[] { AppConfig.class, PersistenceConfig.class };
    }
 
    @Override
    protected Class<?>[] getServletConfigClasses() {
        // contexte web : contrôleurs, configuration MVC
        return new Class<?>[] { WebConfig.class };
    }
 
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
```
 
---

### 44. Quelles sont les étapes de traitement d'une requête REST dans Spring MVC ?

1. Le client envoie une requête HTTP (ex. `POST /api/users` avec un corps JSON).
2. Le `DispatcherServlet` la reçoit et trouve le contrôleur via le `HandlerMapping`.
3. Le corps JSON est **désérialisé** en objet Java (`@RequestBody`).
4. L'objet est **validé** (`@Valid`).
5. Le **contrôleur** appelle le **service**, qui appelle le **repository**, qui interroge la **base de données**.
6. Le résultat remonte au contrôleur, qui renvoie un objet ou un `ResponseEntity`.
7. Le résultat est **sérialisé en JSON**.
8. La **réponse HTTP** (code de statut + corps JSON) est envoyée au client.
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
 
    private final UserService userService;
 
    public UserController(UserService userService) {
        this.userService = userService;
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
 
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto dto) {
        UserDto created = userService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```
 
---

### 45. Comment se fait la sérialisation et désérialisation des objets JSON ?

Spring utilise des **`HttpMessageConverter`** qui s'appuient sur la bibliothèque **Jackson** :

- **Désérialisation** : JSON → objet Java (`@RequestBody`).
- **Sérialisation** : objet Java → JSON (`@ResponseBody` ou `@RestController`).
  Fonctionnement :
- Il suffit d'ajouter **Jackson** aux dépendances : avec `@EnableWebMvc`, Spring enregistre automatiquement le convertisseur JSON.
- Les en-têtes `Content-Type` (format envoyé) et `Accept` (format attendu) permettent de choisir le bon convertisseur.
- On peut personnaliser le résultat avec les annotations Jackson : `@JsonIgnore`, `@JsonProperty`, `@JsonFormat`.
> **Bonne pratique :** ne pas renvoyer directement les entités JPA, mais des **DTO**. Cela évite les boucles infinies (relations bidirectionnelles), les problèmes de chargement paresseux (*lazy loading*) et l'exposition de champs sensibles.
 
---

### 46. À quoi sert un `@RestControllerAdvice` ?

C'est une classe qui **centralise la gestion des exceptions** pour tous les contrôleurs REST. Elle combine `@ControllerAdvice` et `@ResponseBody`. Chaque méthode annotée `@ExceptionHandler` traite un type d'exception et renvoie une **réponse JSON cohérente** avec le bon code HTTP, sans répéter les `try/catch` dans chaque contrôleur.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }
 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
    }
 
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Violation d'une contrainte (ex. valeur déjà existante)"));
    }
}
 
public record ErrorResponse(int status, String message) { }
```
 
---

### 47. Quelles sont les bonnes pratiques pour organiser les packages d'un projet MVC ?

Deux organisations courantes :

**1. Par couche** (classique) :
```
com.example.app
├── config          → AppConfig, PersistenceConfig, WebConfig, WebAppInitializer
├── controller      → contrôleurs REST
├── service         → logique métier
├── repository      → interfaces Spring Data
├── model (entity)  → entités JPA
├── dto             → objets d'échange avec le client
├── mapper          → conversion entité ↔ DTO
├── exception       → exceptions métier + GlobalExceptionHandler
└── util            → classes utilitaires
```

**2. Par fonctionnalité** (utile pour les gros projets) :
```
com.example.app
├── user      → UserController, UserService, UserRepository, User, UserDto
├── product   → ProductController, ProductService, ...
└── config
```

**Règles à respecter :**
- Le **contrôleur** ne contient **pas de logique métier** : il délègue au service.
- Le contrôleur **n'appelle jamais directement le repository**.
- Les dépendances vont **dans un seul sens** : `controller → service → repository`.
- Utiliser des **DTO** plutôt que d'exposer les entités.
- Mettre les **transactions** (`@Transactional`) dans la couche **service**.
- Garder la **configuration** dans un package dédié.
- Limiter le `@ComponentScan` aux packages utiles (contrôleurs dans le contexte web, le reste dans le contexte racine).
- Respecter les conventions de nommage : `UserController`, `UserService`, `UserRepository`.
---

## Récapitulatif de l'architecture (sans Spring Boot)
